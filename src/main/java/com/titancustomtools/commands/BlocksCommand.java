package com.titancustomtools.commands;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.managers.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlocksCommand implements CommandExecutor {

    private final TitanCustomTools plugin;

    public BlocksCommand(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can check blocks.");
            return true;
        }

        Player senderPlayer = (Player) sender;
        OfflinePlayer target = senderPlayer;

        // Logic for checking other players
        if (args.length > 0) {
            // Checks for specific permission OR admin permission OR Op status
            if (sender.hasPermission("titancustomtools.blocks.others") || sender.hasPermission("titancustomtools.admin") || sender.isOp()) {
                target = Bukkit.getOfflinePlayer(args[0]);

                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Player " + args[0] + " has never played before.");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to check others.");
                return true;
            }
        }

        StatsManager stats = plugin.getStatsManager();
        long total = stats.getTotalBlocks(target);

        // Determine display name (You vs PlayerName)
        String name = (target.getUniqueId().equals(senderPlayer.getUniqueId())) ? "You" : target.getName();

        String msg = String.format("&e%s broken: %,d",
                name.equals("You") ? "Total blocks" : name + "'s total blocks",
                total);

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

        return true;
    }
}