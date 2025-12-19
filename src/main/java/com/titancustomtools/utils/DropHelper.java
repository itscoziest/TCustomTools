package com.titancustomtools.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.titanutils.TitanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class DropHelper {

    /**
     * Helper method to check if a location is inside a region starting with "Mine_"
     */
    private static boolean isInsideMineRegion(Location location) {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            return true;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

            for (ProtectedRegion region : set) {
                if (region.getId().toLowerCase().startsWith("mine_")) {
                    return true;
                }
            }
        } catch (NoClassDefFoundError | Exception e) {
            return false;
        }

        return false;
    }

    public static void handleDrop(Player player, ItemStack item, Location location) {
        if (item == null || item.getAmount() <= 0) return;

        boolean addedToInventory = false;

        try {
            if (Bukkit.getPluginManager().isPluginEnabled("TitanUtils")) {
                TitanUtils titanUtils = (TitanUtils) Bukkit.getPluginManager().getPlugin("TitanUtils");

                if (titanUtils != null && titanUtils.getAutoPickupManager() != null) {

                    if (titanUtils.getAutoPickupManager().isAutoPickupEnabled(player) && isInsideMineRegion(location)) {

                        HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                        addedToInventory = true;

                        if (!remaining.isEmpty()) {
                            for (ItemStack leftover : remaining.values()) {
                                location.getWorld().dropItemNaturally(location, leftover);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[TitanCustomTools] Error checking AutoPickup status: " + e.getMessage());
            e.printStackTrace();
        }

        if (!addedToInventory) {
            location.getWorld().dropItemNaturally(location, item);
        }
    }
}