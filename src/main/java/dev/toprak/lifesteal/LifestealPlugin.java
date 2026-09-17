package dev.toprak.lifesteal;

import dev.toprak.lifesteal.command.LifestealCommand;
import dev.toprak.lifesteal.command.WithdrawCommand;
import dev.toprak.lifesteal.listener.HeartItemListener;
import dev.toprak.lifesteal.listener.PlayerDeathListener;
import dev.toprak.lifesteal.manager.HeartManager;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class LifestealPlugin extends JavaPlugin {
    private static LifestealPlugin instance;
    private HeartManager heartManager;
    private NamespacedKey heartKey;
    private NamespacedKey eliminatedKey;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.heartKey = new NamespacedKey(this, "heart_item");
        this.eliminatedKey = new NamespacedKey(this, "eliminated");
        this.heartManager = new HeartManager(this);
        this.heartManager.registerHeartRecipe();

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new HeartItemListener(this), this);

        registerCommand("lifesteal", new LifestealCommand(this));
        registerCommand("withdrawheart", new WithdrawCommand(this));

        getLogger().info("TovekLifesteal v" + getPluginMeta().getVersion() + " loaded.");
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            if (executor instanceof org.bukkit.command.CommandExecutor ce) {
                cmd.setExecutor(ce);
            }
            if (executor instanceof org.bukkit.command.TabCompleter tc) {
                cmd.setTabCompleter(tc);
            }
        }
    }

    public static LifestealPlugin getInstance() {
        return instance;
    }

    public HeartManager getHeartManager() {
        return heartManager;
    }

    public NamespacedKey getHeartKey() {
        return heartKey;
    }

    public NamespacedKey getEliminatedKey() {
        return eliminatedKey;
    }
}
