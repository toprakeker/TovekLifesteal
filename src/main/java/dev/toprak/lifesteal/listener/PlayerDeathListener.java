package dev.toprak.lifesteal.listener;

import dev.toprak.lifesteal.LifestealPlugin;
import dev.toprak.lifesteal.manager.HeartManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Duration;
import java.util.Date;

public class PlayerDeathListener implements Listener {
    private final LifestealPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlayerDeathListener(LifestealPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        HeartManager hearts = plugin.getHeartManager();

        // An already-eliminated player (spectator killed via /kill, etc.) must never feed hearts to anyone.
        if (hearts.isEliminated(victim)) return;

        Player killer = victim.getKiller();
        boolean validKiller = killer != null
                && killer.isOnline()
                && !killer.getUniqueId().equals(victim.getUniqueId())
                && !hearts.isEliminated(killer);

        int minHearts = hearts.getMinHearts();
        int currentVictimHearts = hearts.getHearts(victim);
        int newVictimHearts = currentVictimHearts - 1;
        boolean eliminated = newVictimHearts < minHearts;

        hearts.setHearts(victim, Math.max(minHearts, newVictimHearts));

        if (validKiller) {
            boolean gained = hearts.addHeart(killer);
            if (!gained) {
                victim.getWorld().dropItemNaturally(victim.getLocation(), hearts.createHeartItem(1));
            }

            killer.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-killed", "")
                    .replace("%victim%", victim.getName())));
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

            victim.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.player-died", "")
                    .replace("%killer%", killer.getName())));
        } else {
            victim.getWorld().dropItemNaturally(victim.getLocation(), hearts.createHeartItem(1));
        }

        if (eliminated) {
            handleElimination(victim);
        }
    }

    /** Re-apply elimination on respawn so a relog/death-screen disconnect cannot skip it. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getHeartManager().isEliminated(player)) return;
        Bukkit.getScheduler().runTask(plugin, () -> applyEliminationState(player, false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getHeartManager().isEliminated(player)) return;
        Bukkit.getScheduler().runTask(plugin, () -> applyEliminationState(player, false));
    }

    private void handleElimination(Player player) {
        plugin.getHeartManager().setEliminated(player, true);

        String bcast = plugin.getConfig().getString("elimination-broadcast", "")
                .replace("%player%", player.getName());
        if (!bcast.isBlank()) {
            Bukkit.broadcast(mm.deserialize(bcast));
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> applyEliminationState(player, true), 1L);
    }

    private void applyEliminationState(Player player, boolean showTitle) {
        if (!player.isOnline()) return;
        if (!plugin.getHeartManager().isEliminated(player)) return;

        String action = plugin.getConfig().getString("elimination-action", "SPECTATOR");
        if ("BAN".equalsIgnoreCase(action)) {
            Component reason = mm.deserialize("<red><bold>ELIMINATED</bold></red><newline><gray>You ran out of hearts.</gray>");
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                    "Eliminated: ran out of hearts", (Date) null, "TovekLifesteal");
            player.kick(reason);
            return;
        }

        // Default (and any unknown action): spectator. Never leave an eliminated player alive in survival.
        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SPECTATOR);
        }
        if (showTitle) {
            player.showTitle(Title.title(
                mm.deserialize("<red><bold>ELIMINATED</bold></red>"),
                mm.deserialize("<gray>You ran out of hearts.</gray>"),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))
            ));
        }
    }
}
