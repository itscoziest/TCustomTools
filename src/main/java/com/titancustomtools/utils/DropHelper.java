package com.titancustomtools.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.titanutils.TitanUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DropHelper {

    private static TitanUtils getTitanUtils() {
        Plugin pl = Bukkit.getPluginManager().getPlugin("TitanUtils");
        if (pl != null && pl.isEnabled() && pl instanceof TitanUtils) {
            return (TitanUtils) pl;
        }
        return null;
    }

    private static boolean isInsideMineRegion(Location location) {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) return true;

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

    private static void alertFullInventory(Player player, ItemStack item) {
        if (!player.getInventory().contains(item.getType())) {
            return;
        }
        player.sendTitle(ChatColor.RED + "Inventory is full", ChatColor.GRAY + "Clear some space!", 0, 20, 10);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
    }

    public static void handleDrop(Player player, ItemStack item, Location location) {
        if (item == null || item.getAmount() <= 0) return;

        boolean addedToInventory = false;

        try {
            TitanUtils utils = getTitanUtils();

            if (utils != null && utils.getAutoPickupManager() != null) {

                // FILTER CHECK: If filtered, RETURN immediately.
                // This skips the inventory add AND the fallback drop. Item Deleted.
                if (utils.getAutoPickupManager().isFiltered(player, item.getType())) {
                    return;
                }

                if (utils.getAutoPickupManager().isAutoPickupEnabled(player) && isInsideMineRegion(location)) {

                    HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
                    addedToInventory = true;

                    if (!remaining.isEmpty()) {
                        for (ItemStack leftover : remaining.values()) {
                            location.getWorld().dropItemNaturally(location, leftover);
                            alertFullInventory(player, leftover);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!addedToInventory) {
            location.getWorld().dropItemNaturally(location, item);
        }
    }
}