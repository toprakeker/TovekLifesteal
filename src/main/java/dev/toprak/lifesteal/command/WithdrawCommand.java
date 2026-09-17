package dev.toprak.lifesteal.command;

import dev.toprak.lifesteal.LifestealPlugin;
import dev.toprak.lifesteal.manager.HeartManager;
import dev.toprak.lifesteal.manager.HeartMath;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WithdrawCommand implements CommandExecutor, TabCompleter {
    private final LifestealPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WithdrawCommand(LifestealPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can withdraw hearts.");
            return true;
        }

        HeartManager hearts = plugin.getHeartManager();

        // Dead, spectating or eliminated players cannot withdraw: their heart count is mid-transition.
        if (player.isDead() || player.getGameMode() == GameMode.SPECTATOR || hearts.isEliminated(player)) {
            player.sendMessage(mm.deserialize("<red>You cannot withdraw hearts right now.</red>"));
            return true;
        }

        int amount = 1;
        if (args.length > 0) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount <= 0 || amount > HeartMath.HARD_MAX_HEARTS) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                player.sendMessage(mm.deserialize("<red>Please enter a valid positive number.</red>"));
                return true;
            }
        }

        int minHearts = hearts.getMinHearts();
        int currentHearts = hearts.getHearts(player);

        if (!HeartMath.canWithdraw(currentHearts, amount, minHearts)) {
            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.not-enough-hearts", "")
                    .replace("%min%", String.valueOf(minHearts))));
            return true;
        }

        // Deduct first, then hand out items; giveHeartItems splits into legal stacks and drops overflow.
        hearts.setHearts(player, currentHearts - amount);
        hearts.giveHeartItems(player, amount);

        player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.heart-withdrawn", "")
                .replace("%amount%", String.valueOf(amount))));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("1", "2", "5");
        }
        return List.of();
    }
}
