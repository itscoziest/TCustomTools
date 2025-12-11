package com.titancustomtools.commands;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import com.titancustomtools.managers.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TitanPickCommand implements CommandExecutor {

    private final TitanCustomTools plugin;
    private final ToolManager toolManager;

    public TitanPickCommand(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("titancustomtools.give")) {
            sender.sendMessage(toolManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /titanpick <player> <type>");
            sender.sendMessage(ChatColor.GRAY + "Types: smelter, lumberjack, explosive, block, bountiful");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(toolManager.getMessage("player-not-found"));
            return true;
        }

        ToolType toolType = ToolType.fromString(args[1]);
        if (toolType == null) {
            sender.sendMessage(ChatColor.RED + "Invalid tool type!");
            sender.sendMessage(ChatColor.GRAY + "Types: smelter, lumberjack, explosive, block, bountiful");
            return true;
        }

        ItemStack tool = toolManager.createTool(toolType);
        if (tool == null) {
            sender.sendMessage(ChatColor.RED + "This tool is disabled in the config!");
            return true;
        }

        target.getInventory().addItem(tool);
        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a " + toolType.getDisplayName() + "!");
        target.sendMessage(ChatColor.GREEN + "You received a " + toolType.getDisplayName() + "!");

        return true;
    }
}