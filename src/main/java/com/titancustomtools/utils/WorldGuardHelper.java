package com.titancustomtools.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class WorldGuardHelper {

    private static boolean hooked = false;

    public static void initialize(Plugin plugin) {
        Plugin wg = plugin.getServer().getPluginManager().getPlugin("WorldGuard");

        if (wg == null || !wg.isEnabled()) {
            plugin.getLogger().severe("WorldGuard NOT found! Explosive pickaxe will NOT break blocks (Safety Mode).");
            hooked = false;
            return;
        }

        hooked = true;
        plugin.getLogger().info("Successfully hooked into WorldGuard!");
    }

    public static boolean isInMiningRegion(Location location, List<String> allowedNames) {
        if (!hooked) {
            // If WorldGuard is missing, return false (Don't explode anything)
            return false;
        }

        try {
            // Direct API Access (No reflection)
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

            for (ProtectedRegion region : set) {
                String regionId = region.getId().toLowerCase();

                // Check if region name contains any of the allowed words from config
                for (String allowed : allowedNames) {
                    if (regionId.contains(allowed.toLowerCase())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}