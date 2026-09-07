package dev.toprak.lifesteal.listener;

import dev.toprak.lifesteal.LifestealPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.time.Duration;

public class PlayerDeathListener implements Listener {
    private final LifestealPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlayerDeathListener(LifestealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        int currentVictimHearts = plugin.getHeartManager().getHearts(victim);
        int newVictimHearts = currentVictimHearts - 1;
        int minHearts = plugin.getConfig().getInt("min-hearts", 1);
        
        plugin.getHeartManager().setHearts(victim, Math.max(minHearts, newVictimHearts));

        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            boolean gained = plugin.getHeartManager().addHeart(killer);
            if (!gained) {
                victim.getWorld().dropItemNaturally(victim.getLocation(), plugin.getHeartManager().createHeartItem(1));
            }

            killer.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-killed", "")
                    .replace("%victim%", victim.getName())));
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

            victim.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-died", "")
                    .replace("%killer%", killer.getName())));
        } else {
            victim.getWorld().dropItemNaturally(victim.getLocation(), plugin.getHeartManager().createHeartItem(1));
        }

        if (newVictimHearts < minHearts) {
            handleElimination(victim);
        }
    }

    private void handleElimination(Player player) {
        String action = plugin.getConfig().getString("elimination-action", "SPECTATOR");
        String bcast = plugin.getConfig().getString("elimination-broadcast", "")
                .replace("%player%", player.getName());

        if (!bcast.isBlank()) {
            Bukkit.broadcast(mm.deserialize(bcast));
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            if ("SPECTATOR".equalsIgnoreCase(action)) {
                player.setGameMode(GameMode.SPECTATOR);
                player.showTitle(Title.title(
                    mm.deserialize("<red><bold>ELIMINATED</bold></red>"),
                    mm.deserialize("<gray>You ran out of hearts.</gray>"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))
                ));
            }
        }, 1L);
    }
}
