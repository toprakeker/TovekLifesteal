package dev.toprak.lifesteal.manager;

import dev.toprak.lifesteal.LifestealPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class HeartManager {
    private final LifestealPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public HeartManager(LifestealPlugin plugin) {
        this.plugin = plugin;
    }

    public int getHearts(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return 10;
        return (int) Math.round(attr.getBaseValue() / 2.0);
    }

    public void setHearts(Player player, int hearts) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            double health = Math.max(2.0, hearts * 2.0);
            attr.setBaseValue(health);
            if (player.getHealth() > health) {
                player.setHealth(health);
            }
        }
    }

    public boolean addHeart(Player player) {
        int max = plugin.getConfig().getInt("max-hearts", 20);
        int current = getHearts(player);
        if (current >= max) {
            return false;
        }
        setHearts(player, current + 1);
        return true;
    }

    public ItemStack createHeartItem(int amount) {
        String matName = plugin.getConfig().getString("heart-item-material", "NETHER_STAR");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.NETHER_STAR;

        ItemStack item = new ItemStack(mat, Math.max(1, amount));
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

    public boolean isHeartItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
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
        recipe.setIngredient('S', Material.NETHER_STAR);

        Bukkit.addRecipe(recipe);
    }
}
