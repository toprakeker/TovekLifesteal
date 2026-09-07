package dev.toprak.lifesteal.listener;

import dev.toprak.lifesteal.LifestealPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class HeartItemListener implements Listener {
    private final LifestealPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public HeartItemListener(LifestealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!plugin.getHeartManager().isHeartItem(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        boolean added = plugin.getHeartManager().addHeart(player);
        if (!added) {
            int max = plugin.getConfig().getInt("max-hearts", 20);
            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.heart-maxed", "")
                    .replace("%max%", String.valueOf(max))));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Safely consume 1 item from whatever hand was clicked
        if (event.getHand() == EquipmentSlot.HAND) {
            ItemStack main = player.getInventory().getItemInMainHand();
            if (plugin.getHeartManager().isHeartItem(main)) {
                main.subtract(1);
            }
        } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
            ItemStack off = player.getInventory().getItemInOffHand();
            if (plugin.getHeartManager().isHeartItem(off)) {
                off.subtract(1);
            }
        }

        player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.heart-gained", "")));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
    }
}
