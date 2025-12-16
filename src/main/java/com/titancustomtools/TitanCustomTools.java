package com.titancustomtools;

import com.titancustomtools.commands.TitanPickCommand;
import com.titancustomtools.listeners.StatsListener;
import com.titancustomtools.listeners.ToolListener;
import com.titancustomtools.listeners.ToolProtectionListener;
import com.titancustomtools.managers.StatsManager;
import com.titancustomtools.managers.ToolManager;
import com.titancustomtools.utils.TitanPlaceholders;
import com.titancustomtools.utils.WorldGuardHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class TitanCustomTools extends JavaPlugin {

    private static TitanCustomTools instance;
    private ToolManager toolManager;
    private StatsManager statsManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.toolManager = new ToolManager(this);
        this.statsManager = new StatsManager(this);

        WorldGuardHelper.initialize(this);

        getCommand("titanpick").setExecutor(new TitanPickCommand(this));

        // Register Events
        getServer().getPluginManager().registerEvents(new ToolListener(this), this);
        getServer().getPluginManager().registerEvents(new StatsListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolProtectionListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TitanPlaceholders(this).register();
            getLogger().info("Hooked into PlaceholderAPI!");
        }

        getLogger().info("TitanCustomTools has been enabled!");
    }

    @Override
    public void onDisable() {
        if (statsManager != null) {
            statsManager.shutdown();
        }
        getLogger().info("TitanCustomTools has been disabled!");
    }

    public static TitanCustomTools getInstance() {
        return instance;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public void reloadConfiguration() {
        reloadConfig();
        toolManager = new ToolManager(this);
    }
}