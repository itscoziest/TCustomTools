package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.utils.DropHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LumberjackAbility {

    private final TitanCustomTools plugin;
    private static final Set<Material> LOG_TYPES = new HashSet<>();
    private static final Set<Material> LEAF_TYPES = new HashSet<>();

    static {
        LOG_TYPES.add(Material.OAK_LOG);
        LOG_TYPES.add(Material.SPRUCE_LOG);
        LOG_TYPES.add(Material.BIRCH_LOG);
        LOG_TYPES.add(Material.JUNGLE_LOG);
        LOG_TYPES.add(Material.ACACIA_LOG);
        LOG_TYPES.add(Material.DARK_OAK_LOG);
        LOG_TYPES.add(Material.MANGROVE_LOG);
        LOG_TYPES.add(Material.CHERRY_LOG);
        LOG_TYPES.add(Material.CRIMSON_STEM);
        LOG_TYPES.add(Material.WARPED_STEM);
        LOG_TYPES.add(Material.OAK_WOOD);
        LOG_TYPES.add(Material.SPRUCE_WOOD);
        LOG_TYPES.add(Material.BIRCH_WOOD);
        LOG_TYPES.add(Material.JUNGLE_WOOD);
        LOG_TYPES.add(Material.ACACIA_WOOD);
        LOG_TYPES.add(Material.DARK_OAK_WOOD);
        LOG_TYPES.add(Material.MANGROVE_WOOD);
        LOG_TYPES.add(Material.CHERRY_WOOD);
        LOG_TYPES.add(Material.CRIMSON_HYPHAE);
        LOG_TYPES.add(Material.WARPED_HYPHAE);
        LOG_TYPES.add(Material.STRIPPED_OAK_LOG);
        LOG_TYPES.add(Material.STRIPPED_SPRUCE_LOG);
        LOG_TYPES.add(Material.STRIPPED_BIRCH_LOG);
        LOG_TYPES.add(Material.STRIPPED_JUNGLE_LOG);
        LOG_TYPES.add(Material.STRIPPED_ACACIA_LOG);
        LOG_TYPES.add(Material.STRIPPED_DARK_OAK_LOG);
        LOG_TYPES.add(Material.STRIPPED_MANGROVE_LOG);
        LOG_TYPES.add(Material.STRIPPED_CHERRY_LOG);
        LOG_TYPES.add(Material.STRIPPED_CRIMSON_STEM);
        LOG_TYPES.add(Material.STRIPPED_WARPED_STEM);

        LEAF_TYPES.add(Material.OAK_LEAVES);
        LEAF_TYPES.add(Material.SPRUCE_LEAVES);
        LEAF_TYPES.add(Material.BIRCH_LEAVES);
        LEAF_TYPES.add(Material.JUNGLE_LEAVES);
        LEAF_TYPES.add(Material.ACACIA_LEAVES);
        LEAF_TYPES.add(Material.DARK_OAK_LEAVES);
        LEAF_TYPES.add(Material.MANGROVE_LEAVES);
        LEAF_TYPES.add(Material.CHERRY_LEAVES);
        LEAF_TYPES.add(Material.AZALEA_LEAVES);
        LEAF_TYPES.add(Material.FLOWERING_AZALEA_LEAVES);
        LEAF_TYPES.add(Material.NETHER_WART_BLOCK);
        LEAF_TYPES.add(Material.WARPED_WART_BLOCK);
        LEAF_TYPES.add(Material.SHROOMLIGHT);
    }

    public LumberjackAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleLumberjack(BlockBreakEvent event, Player player, Block block, ItemStack tool) {
        Material blockType = block.getType();

        if (!LOG_TYPES.contains(blockType)) {
            return;
        }

        boolean checkLeaves = plugin.getConfig().getBoolean("lumberjack-axe.check-leaves", true);
        int maxHeight = plugin.getConfig().getInt("lumberjack-axe.max-tree-height", 32);

        if (checkLeaves && !hasLeavesAbove(block, maxHeight)) {
            return;
        }

        if (!isBottomLog(block)) {
            return;
        }

        Set<Block> logs = findConnectedLogs(block, blockType, maxHeight);

        if (logs.isEmpty()) {
            return;
        }

        Set<Block> leavesToRemove = new HashSet<>();

        for (Block log : logs) {
            if (log.equals(block)) continue;

            BlockBreakEvent breakEvent = new BlockBreakEvent(log, player);
            Bukkit.getPluginManager().callEvent(breakEvent);

            if (!breakEvent.isCancelled()) {
                Collection<ItemStack> drops = log.getDrops(tool, player);

                log.setType(Material.AIR);

                for (ItemStack drop : drops) {
                    DropHelper.handleDrop(player, drop, log.getLocation());
                }

                plugin.getStatsManager().incrementTotal(player, 1);

                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            Block nearbyBlock = log.getRelative(x, y, z);
                            if (LEAF_TYPES.contains(nearbyBlock.getType())) {
                                leavesToRemove.add(nearbyBlock);
                            }
                        }
                    }
                }
            }
        }

        for (Block leaf : leavesToRemove) {
            if (!LEAF_TYPES.contains(leaf.getType())) continue;

            Collection<ItemStack> drops = leaf.getDrops(tool, player);
            leaf.setType(Material.AIR);

            for (ItemStack drop : drops) {
                DropHelper.handleDrop(player, drop, leaf.getLocation());
            }
        }
    }

    private boolean isBottomLog(Block block) {
        Block below = block.getRelative(BlockFace.DOWN);
        return !LOG_TYPES.contains(below.getType());
    }

    private boolean hasLeavesAbove(Block block, int maxHeight) {
        for (int i = 1; i <= maxHeight; i++) {
            Block above = block.getRelative(0, i, 0);

            if (LEAF_TYPES.contains(above.getType())) {
                return true;
            }

            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    Block check = above.getRelative(x, 0, z);
                    if (LEAF_TYPES.contains(check.getType())) {
                        return true;
                    }
                }
            }

            if (!LOG_TYPES.contains(above.getType())) {
                break;
            }
        }
        return false;
    }

    private Set<Block> findConnectedLogs(Block start, Material logType, int maxHeight) {
        Set<Block> logs = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && logs.size() < maxHeight * 4) {
            Block current = queue.poll();

            if (current.getY() - start.getY() > maxHeight) {
                continue;
            }

            logs.add(current);

            for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH,
                    BlockFace.EAST, BlockFace.WEST}) {
                Block adjacent = current.getRelative(face);

                if (!visited.contains(adjacent) && LOG_TYPES.contains(adjacent.getType())) {
                    visited.add(adjacent);
                    queue.add(adjacent);
                }
            }

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue;
                    Block diagonal = current.getRelative(x, 1, z);

                    if (!visited.contains(diagonal) && LOG_TYPES.contains(diagonal.getType())) {
                        visited.add(diagonal);
                        queue.add(diagonal);
                    }
                }
            }
        }

        return logs;
    }
}