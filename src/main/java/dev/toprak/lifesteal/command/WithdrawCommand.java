package dev.toprak.lifesteal.command;

import dev.toprak.lifesteal.LifestealPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

        int amount = 1;
        if (args.length > 0) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                player.sendMessage(mm.deserialize("<red>Please enter a valid positive number.</red>"));
                return true;
            }
        }

        int minHearts = plugin.getConfig().getInt("min-hearts", 1);
        int currentHearts = plugin.getHeartManager().getHearts(player);

        if (currentHearts - amount < minHearts) {
            player.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.not-enough-hearts", "")
                    .replace("%min%", String.valueOf(minHearts))));
            return true;
        }

        plugin.getHeartManager().setHearts(player, currentHearts - amount);
        ItemStack heartItem = plugin.getHeartManager().createHeartItem(amount);
        var leftover = player.getInventory().addItem(heartItem);
        leftover.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));

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
