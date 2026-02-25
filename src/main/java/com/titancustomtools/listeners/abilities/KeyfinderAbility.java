package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class KeyfinderAbility {
    private final TitanCustomTools plugin;
    private final Random random = new Random();
    private final NamespacedKey blocksKey;

    public KeyfinderAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
        // Single tracker for the milestone
        this.blocksKey = new NamespacedKey(plugin, "keyfinder_blocks_mined");
    }

    public void handleKeyfinder(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        // Target amount (defaults to 1000 if not in config)
        int requiredBlocks = plugin.getConfig().getInt("keyfinder-pickaxe.blocks-required", 1000);
        int currentBlocks = pdc.getOrDefault(blocksKey, PersistentDataType.INTEGER, 0);

        // Add 1 block mined
        currentBlocks++;

        // Check if they hit the milestone
        if (currentBlocks >= requiredBlocks) {
            rollAndGiveKey(player);
            // Reset counter
            currentBlocks = 0;
        }

        // Save their updated block count
        pdc.set(blocksKey, PersistentDataType.INTEGER, currentBlocks);

        // Send the action bar message
        int blocksLeft = requiredBlocks - currentBlocks;
        String actionBarMsg =
                ChatColor.YELLOW + "" + ChatColor.BOLD + blocksLeft +
                        ChatColor.GOLD + " blocks until next key";
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMsg));
    }

    private void rollAndGiveKey(Player player) {
        int roll = random.nextInt(100); // Roll 0-99
        String keyType;

        if (roll < 70) {
            // 0-69: 70% chance
            keyType = "fish";
        } else if (roll < 90) {
            // 70-89: 20% chance
            keyType = "rebirth";
        } else {
            // 90-99: 10% chance
            keyType = "titan";
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate key give " + player.getName() + " " + keyType + " 1");
        player.sendMessage(ChatColor.GREEN + "You found a " + keyType.toUpperCase() + " key!");
    }
}