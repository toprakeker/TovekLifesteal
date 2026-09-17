package dev.toprak.lifesteal.command;

import dev.toprak.lifesteal.LifestealPlugin;
import dev.toprak.lifesteal.manager.HeartManager;
import dev.toprak.lifesteal.manager.HeartMath;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class LifestealCommand implements CommandExecutor, TabCompleter {
    private final LifestealPlugin plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public LifestealCommand(LifestealPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lifesteal.admin")) {
            sender.sendMessage(mm.deserialize("<red>You don't have permission to use this command.</red>"));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.getHeartManager().registerHeartRecipe();
                sender.sendMessage(mm.deserialize("<green>TovekLifesteal configuration reloaded.</green>"));
            }
            case "give" -> {
                if (args.length < 2) {
                    sender.sendMessage(mm.deserialize("<red>Usage: /lifesteal give <player> [amount]</red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Player not found.</red>"));
                    return true;
                }
                int count = 1;
                if (args.length > 2) {
                    try {
                        count = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(mm.deserialize("<red>Invalid amount.</red>"));
                        return true;
                    }
                }
                if (count < 1 || count > HeartManager.MAX_ITEMS_PER_OPERATION) {
                    sender.sendMessage(mm.deserialize("<red>Amount must be between 1 and "
                            + HeartManager.MAX_ITEMS_PER_OPERATION + ".</red>"));
                    return true;
                }
                int given = plugin.getHeartManager().giveHeartItems(target, count);
                sender.sendMessage(mm.deserialize("<green>Gave " + given + " heart item(s) to " + target.getName() + ".</green>"));
            }
            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage(mm.deserialize("<red>Usage: /lifesteal set <player> <hearts></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Player not found.</red>"));
                    return true;
                }
                try {
                    int requested = Integer.parseInt(args[2]);
                    int hearts = HeartMath.clampHearts(requested);
                    plugin.getHeartManager().setHearts(target, hearts);
                    sender.sendMessage(mm.deserialize("<green>Set " + target.getName() + "'s hearts to " + hearts + ".</green>"));
                } catch (NumberFormatException e) {
                    sender.sendMessage(mm.deserialize("<red>Invalid heart amount.</red>"));
                }
            }
            case "revive" -> {
                if (args.length < 2) {
                    sender.sendMessage(mm.deserialize("<red>Usage: /lifesteal revive <player></red>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(mm.deserialize("<red>Player not found.</red>"));
                    return true;
                }
                if (!plugin.getHeartManager().isEliminated(target) && target.getGameMode() != GameMode.SPECTATOR) {
                    sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.already-alive", "<red>Player is not eliminated.</red>")));
                    return true;
                }
                int defHearts = plugin.getHeartManager().getDefaultHearts();
                plugin.getHeartManager().setEliminated(target, false);
                target.setGameMode(GameMode.SURVIVAL);
                plugin.getHeartManager().setHearts(target, defHearts);
                target.teleport(target.getWorld().getSpawnLocation());
                target.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.revived", "")
                        .replace("%reviver%", sender.getName())
                        .replace("%hearts%", String.valueOf(defHearts))));
                sender.sendMessage(mm.deserialize(plugin.getConfig().getString("messages.target-revived", "")
                        .replace("%target%", target.getName())));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<gradient:red:gold><bold>TovekLifesteal Admin</bold></gradient>"));
        sender.sendMessage(mm.deserialize("<gray>/lifesteal reload</gray> - Reload configs"));
        sender.sendMessage(mm.deserialize("<gray>/lifesteal give <player> [amt]</gray> - Give heart items"));
        sender.sendMessage(mm.deserialize("<gray>/lifesteal set <player> <amt></gray> - Set player's max hearts"));
        sender.sendMessage(mm.deserialize("<gray>/lifesteal revive <player></gray> - Revive an eliminated player"));
        sender.sendMessage(mm.deserialize("<gray>/withdrawheart [amt]</gray> - Withdraw hearts"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("reload", "give", "set", "revive").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
