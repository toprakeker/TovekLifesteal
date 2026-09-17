package dev.toprak.lifesteal.listener;

import dev.toprak.lifesteal.LifestealPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class HeartItemListener implements Listener {
    private final LifestealPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public HeartItemListener(LifestealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        EquipmentSlot hand = event.getHand();
        if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK
                && action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return;
        if (!plugin.getHeartManager().isHeartItem(event.getItem())) return;

        Player player = event.getPlayer();
        // Always swallow the click for heart items so they can never break blocks or place/use base material
        event.setCancelled(true);

        if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR
                || plugin.getHeartManager().isEliminated(player)) {
            return;
        }

        // Re-read the live slot rather than trusting the event snapshot; the slot is the source of truth.
        PlayerInventory inv = player.getInventory();
        ItemStack live = hand == EquipmentSlot.HAND ? inv.getItemInMainHand() : inv.getItemInOffHand();
        if (!plugin.getHeartManager().isHeartItem(live) || live.getAmount() <= 0) return;

        if (!plugin.getHeartManager().addHeart(player)) {
            int max = plugin.getHeartManager().getMaxHearts();
            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.heart-maxed", "")
                    .replace("%max%", String.valueOf(max))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Consume exactly one from the clicked slot and write the slot back explicitly.
        int remaining = live.getAmount() - 1;
        ItemStack replacement = remaining > 0 ? live.clone() : null;
        if (replacement != null) replacement.setAmount(remaining);
        if (hand == EquipmentSlot.HAND) {
            inv.setItemInMainHand(replacement);
        } else {
            inv.setItemInOffHand(replacement);
        }

        player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.heart-gained", "")));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
    }
}
