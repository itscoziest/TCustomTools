package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BountifulAbility {

    private final TitanCustomTools plugin;
    private final Random random;
    private static final Set<Material> ORE_TYPES = new HashSet<>();

    static {
        ORE_TYPES.add(Material.COAL_ORE);
        ORE_TYPES.add(Material.DEEPSLATE_COAL_ORE);
        ORE_TYPES.add(Material.IRON_ORE);
        ORE_TYPES.add(Material.DEEPSLATE_IRON_ORE);
        ORE_TYPES.add(Material.COPPER_ORE);
        ORE_TYPES.add(Material.DEEPSLATE_COPPER_ORE);
        ORE_TYPES.add(Material.GOLD_ORE);
        ORE_TYPES.add(Material.DEEPSLATE_GOLD_ORE);
        ORE_TYPES.add(Material.NETHER_GOLD_ORE);
        ORE_TYPES.add(Material.REDSTONE_ORE);
        ORE_TYPES.add(Material.DEEPSLATE_REDSTONE_ORE);
        ORE_TYPES.add(Material.LAPIS_ORE);
        ORE_TYPES.add(Material.DEEPSLATE_LAPIS_ORE);
        ORE_TYPES.add(Material.DIAMOND_ORE);
        ORE_TYPES.add(Material.DEEPSLATE_DIAMOND_ORE);
        ORE_TYPES.add(Material.EMERALD_ORE);
        ORE_TYPES.add(Material.DEEPSLATE_EMERALD_ORE);
        ORE_TYPES.add(Material.NETHER_QUARTZ_ORE);
        ORE_TYPES.add(Material.ANCIENT_DEBRIS);
        ORE_TYPES.add(Material.RAW_IRON_BLOCK);
        ORE_TYPES.add(Material.RAW_GOLD_BLOCK);
        ORE_TYPES.add(Material.RAW_COPPER_BLOCK);
    }

    public BountifulAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void handleBountiful(BlockBreakEvent event, Player player, Block centerBlock, ItemStack tool) {
        int scanRadius = plugin.getConfig().getInt("bountiful-pickaxe.scan-radius", 1);

        // List to store all valid ores found nearby
        List<Block> nearbyOres = new ArrayList<>();

        // 1. Scan surroundings and collect valid ores
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block neighbor = centerBlock.getRelative(x, y, z);

                    if (ORE_TYPES.contains(neighbor.getType())) {
                        nearbyOres.add(neighbor);
                    }
                }
            }
        }

        // 2. If we found any ores, pick EXACTLY ONE at random
        if (!nearbyOres.isEmpty()) {
            // *** FIX START: Prevent the original block (e.g. Stone) from dropping ***
            event.setDropItems(false);
            // *** FIX END ***

            Block chosenOre = nearbyOres.get(random.nextInt(nearbyOres.size()));

            // 3. Get drops for that single ore (Respects Fortune/Silk Touch)
            Collection<ItemStack> drops = chosenOre.getDrops(tool, player);

            // 4. Drop at center of mined block
            Location dropLoc = centerBlock.getLocation().add(0.5, 0.5, 0.5);
            for (ItemStack drop : drops) {
                centerBlock.getWorld().dropItemNaturally(dropLoc, drop);
            }
        }
    }
}