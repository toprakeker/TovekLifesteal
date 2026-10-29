package dev.toprak.lifesteal.manager;

import dev.toprak.lifesteal.LifestealPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class HeartManager {
    /** Upper bound for a single item give/withdraw so nobody can request billions of items. */
    public static final int MAX_ITEMS_PER_OPERATION = 64 * 36;

    private final LifestealPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public HeartManager(LifestealPlugin plugin) {
        this.plugin = plugin;
    }

    public int getMinHearts() {
        return HeartMath.sanitizeMin(plugin.getConfig().getInt("min-hearts", 1));
    }

    public int getMaxHearts() {
        return HeartMath.sanitizeMax(plugin.getConfig().getInt("max-hearts", 20), getMinHearts());
    }

    public int getDefaultHearts() {
        return HeartMath.clampHearts(plugin.getConfig().getInt("default-hearts", 10), getMinHearts(), getMaxHearts());
    }

    private static Attribute getMaxHealthAttribute() {
        Attribute attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.max_health"));
        if (attr != null) return attr;
        attr = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"));
        if (attr != null) return attr;
        return Registry.ATTRIBUTE.iterator().next();
    }

    public int getHearts(Player player) {
        AttributeInstance attr = player.getAttribute(getMaxHealthAttribute());
        if (attr == null) return getDefaultHearts();
        return HeartMath.healthToHearts(attr.getBaseValue());
    }

    /**
     * Sets the player's max hearts, hard-clamped to [1, 512]. Current health is clamped down to the
     * new maximum only when the player is alive (setHealth on a dead player is invalid).
     */
    public void setHearts(Player player, int hearts) {
        AttributeInstance attr = player.getAttribute(getMaxHealthAttribute());
        if (attr == null) return;
        double health = HeartMath.heartsToHealth(hearts);
        attr.setBaseValue(health);
        if (!player.isDead() && player.getHealth() > health) {
            player.setHealth(health);
        }
    }

    public boolean addHeart(Player player) {
        int max = getMaxHearts();
        int current = getHearts(player);
        if (current >= max) {
            return false;
        }
        setHearts(player, current + 1);
        return true;
    }

    // ---- elimination state (persisted in the player's PDC so it survives relogs/crashes) ----

    public boolean isEliminated(Player player) {
        Byte flag = player.getPersistentDataContainer().get(plugin.getEliminatedKey(), PersistentDataType.BYTE);
        return flag != null && flag == (byte) 1;
    }

    public void setEliminated(Player player, boolean eliminated) {
        if (eliminated) {
            player.getPersistentDataContainer().set(plugin.getEliminatedKey(), PersistentDataType.BYTE, (byte) 1);
        } else {
            player.getPersistentDataContainer().remove(plugin.getEliminatedKey());
        }
    }

    // ---- items ----

    private Material heartMaterial() {
        String matName = plugin.getConfig().getString("heart-item-material", "NETHER_STAR");
        Material mat = Material.matchMaterial(matName);
        if (mat == null || mat.isAir() || !mat.isItem()) mat = Material.NETHER_STAR;
        return mat;
    }

    /** Creates a single stack. Amount is clamped to [1, material max stack size]. */
    public ItemStack createHeartItem(int amount) {
        Material mat = heartMaterial();
        int maxStack = Math.max(1, mat.getMaxStackSize());
        ItemStack item = new ItemStack(mat, Math.max(1, Math.min(maxStack, amount)));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize("<red><bold>Heart Fragment</bold></red>"));
            meta.lore(List.of(
                mm.deserialize("<gray>Right-click to consume and claim</gray>"),
                mm.deserialize("<red>+1 Max Heart</red><gray> permanently.</gray>"),
                mm.deserialize(""),
                mm.deserialize("<dark_gray>Tier: <gold>Ancient Relic</gold></dark_gray>")
            ));
            meta.getPersistentDataContainer().set(plugin.getHeartKey(), PersistentDataType.BYTE, (byte) 1);
            int cmd = plugin.getConfig().getInt("heart-item-model-data", 1001);
            if (cmd > 0) {
                meta.setCustomModelData(cmd);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Gives {@code amount} heart items split into legal stack sizes; anything that does not fit is
     * dropped at the player's feet as legal-sized stacks (never an oversized item entity).
     * Returns the number of items actually created.
     */
    public int giveHeartItems(Player player, int amount) {
        int total = Math.max(0, Math.min(MAX_ITEMS_PER_OPERATION, amount));
        if (total == 0) return 0;
        int maxStack = Math.max(1, heartMaterial().getMaxStackSize());
        for (int size : HeartMath.splitStacks(total, maxStack)) {
            var leftover = player.getInventory().addItem(createHeartItem(size));
            for (ItemStack rest : leftover.values()) {
                for (int drop : HeartMath.splitStacks(rest.getAmount(), maxStack)) {
                    player.getWorld().dropItemNaturally(player.getLocation(), createHeartItem(drop));
                }
            }
        }
        return total;
    }

    public boolean isHeartItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(plugin.getHeartKey(), PersistentDataType.BYTE);
    }

    public void registerHeartRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "craftable_heart");
        Bukkit.removeRecipe(recipeKey);

        ItemStack heart = createHeartItem(1);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, heart);
        recipe.shape("NTN", "TST", "NTN");
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('T', Material.TOTEM_OF_UNDYING);
        // Exact choice: a Heart Fragment (which may itself be a nether star) must not be usable as the
        // core ingredient, otherwise a heart item silently becomes crafting fuel.
        recipe.setIngredient('S', new RecipeChoice.ExactChoice(new ItemStack(Material.NETHER_STAR)));

        Bukkit.addRecipe(recipe);
    }
}
