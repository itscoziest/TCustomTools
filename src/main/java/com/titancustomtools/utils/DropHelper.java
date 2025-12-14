package com.titancustomtools.utils;

import com.titanutils.TitanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class DropHelper {

    public static void handleDrop(Player player, ItemStack item, Location location) {
        if (item == null || item.getAmount() <= 0) return;

        boolean addedToInventory = false;

        // Try to use TitanUtils AutoPickup
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("TitanUtils")) {
                TitanUtils titanUtils = (TitanUtils) Bukkit.getPluginManager().getPlugin("TitanUtils");

                // Ensure titanUtils and the manager are not null before checking
                if (titanUtils != null && titanUtils.getAutoPickupManager() != null) {
                    if (titanUtils.getAutoPickupManager().isAutoPickupEnabled(player)) {

                        HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                        addedToInventory = true; // Flag that we attempted inventory add

                        // Drop whatever didn't fit
                        if (!remaining.isEmpty()) {
                            for (ItemStack leftover : remaining.values()) {
                                location.getWorld().dropItemNaturally(location, leftover);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // If TitanUtils errors out, print a warning but continue to drop naturally below
            Bukkit.getLogger().warning("[TitanCustomTools] Error checking AutoPickup status: " + e.getMessage());
            e.printStackTrace();
        }

        // If we didn't add it to inventory (either because AutoPickup is OFF, TitanUtils is missing, or an error occurred)
        // Then drop it on the ground naturally.
        if (!addedToInventory) {
            location.getWorld().dropItemNaturally(location, item);
        }
    }
}