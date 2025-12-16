package com.titancustomtools.listeners;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ToolProtectionListener implements Listener {

    private final TitanCustomTools plugin;

    // Safety drop: Map stores <PlayerUUID, Timestamp>
    private final Map<UUID, Long> dropConfirmations = new HashMap<>();

    // Keep inventory: Map stores <PlayerUUID, List of Items to give back>
    private final Map<UUID, List<ItemStack>> itemsToRestore = new HashMap<>();

    public ToolProtectionListener(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles keeping specific tools in inventory on death.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // If keepInventory gamerule is already on, we don't need to do anything
        if (event.getEntity().getWorld().getGameRuleValue("keepInventory").equalsIgnoreCase("true")) {
            return;
        }

        List<ItemStack> toKeep = new ArrayList<>();
        Iterator<ItemStack> iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            ItemStack item = iterator.next();

            // Check if it is a Titan Tool
            if (plugin.getToolManager().isCustomTool(item)) {
                // Save it to our list
                toKeep.add(item);
                // Remove from the drops that fall on the ground
                iterator.remove();
            }
        }

        // If we found tools to save, store them in the map for respawn
        if (!toKeep.isEmpty()) {
            itemsToRestore.put(event.getEntity().getUniqueId(), toKeep);
        }
    }

    /**
     * Gives the items back when the player respawns.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (itemsToRestore.containsKey(uuid)) {
            List<ItemStack> items = itemsToRestore.get(uuid);

            for (ItemStack item : items) {
                player.getInventory().addItem(item);
            }

            // Clean up memory
            itemsToRestore.remove(uuid);
        }
    }

    /**
     * Handles the "Double Q" to drop safety feature.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();

        // Only run logic if it is a Titan Tool
        if (!plugin.getToolManager().isCustomTool(item)) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check if player has recently pressed Q (within 3 seconds)
        if (dropConfirmations.containsKey(uuid)) {
            long lastPressTime = dropConfirmations.get(uuid);

            // If strictly less than 3 seconds have passed
            if (currentTime - lastPressTime < 3000) {
                // Remove from map and ALLOW the drop
                dropConfirmations.remove(uuid);
                return;
            }
        }

        // If we reach here, it's the first press or the previous one expired
        event.setCancelled(true);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&cThis is a donor pickaxe, are you sure you want to drop it? press Q again to drop"));

        // Update the map with the current time
        dropConfirmations.put(uuid, currentTime);
    }
}