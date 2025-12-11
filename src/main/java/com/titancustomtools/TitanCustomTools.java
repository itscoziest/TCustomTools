package com.titancustomtools;

import com.titancustomtools.commands.TitanPickCommand;
import com.titancustomtools.listeners.ToolListener;
import com.titancustomtools.managers.ToolManager;
import com.titancustomtools.utils.WorldGuardHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class TitanCustomTools extends JavaPlugin {

    private static TitanCustomTools instance;
    private ToolManager toolManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.toolManager = new ToolManager(this);

        WorldGuardHelper.initialize(this);

        getCommand("titanpick").setExecutor(new TitanPickCommand(this));

        getServer().getPluginManager().registerEvents(new ToolListener(this), this);

        getLogger().info("TitanCustomTools has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("TitanCustomTools has been disabled!");
    }

    public static TitanCustomTools getInstance() {
        return instance;
    }

    public ToolManager getToolManager() {
        return toolManager;
    }

    public void reloadConfiguration() {
        reloadConfig();
        toolManager = new ToolManager(this);
    }
}