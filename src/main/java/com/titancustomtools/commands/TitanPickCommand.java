package com.titancustomtools.commands;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import com.titancustomtools.managers.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TitanPickCommand implements CommandExecutor, TabCompleter {

    private final TitanCustomTools plugin;
    private final ToolManager toolManager;

    public TitanPickCommand(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("titancustomtools.give")) {
            sender.sendMessage(toolManager.getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /titanpick <player> <type> [netherite] [uses]");
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
            return true;
        }

        boolean isNetherite = false;
        if (args.length >= 3) {
            isNetherite = args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("netherite");
        }

        int uses = -1;
        if (args.length >= 4) {
            try {
                uses = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number for uses! Using permanent.");
            }
        }

        ItemStack tool = toolManager.createTool(toolType, isNetherite, uses);

        if (tool == null) {
            sender.sendMessage(ChatColor.RED + "This tool is disabled in the config!");
            return true;
        }

        target.getInventory().addItem(tool);

        String typeName = toolType.getDisplayName();
        String useMsg = (uses > 0) ? " (" + uses + " uses)" : " (Permanent)";

        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " a " + typeName + useMsg);
        target.sendMessage(ChatColor.GREEN + "You received a " + typeName + useMsg);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) {
            return null; // Returns list of online players
        }

        if (args.length == 2) {
            return Arrays.stream(ToolType.values())
                    .map(type -> type.getConfigKey().toLowerCase())
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            return Arrays.asList("true", "false");
        }

        if (args.length == 4) {
            return Arrays.asList("100", "500", "1000", "-1");
        }

        return new ArrayList<>();
    }
}