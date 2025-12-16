package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.utils.DropHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlockAbility {

    private final TitanCustomTools plugin;
    private static final Map<Material, Material> ORE_TO_BLOCK = new HashMap<>();

    static {
        // --- Standard Gems ---
        ORE_TO_BLOCK.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
        ORE_TO_BLOCK.put(Material.EMERALD, Material.EMERALD_BLOCK);
        ORE_TO_BLOCK.put(Material.COAL, Material.COAL_BLOCK);
        ORE_TO_BLOCK.put(Material.REDSTONE, Material.REDSTONE_BLOCK);
        ORE_TO_BLOCK.put(Material.LAPIS_LAZULI, Material.LAPIS_BLOCK);
        ORE_TO_BLOCK.put(Material.QUARTZ, Material.QUARTZ_BLOCK);
        ORE_TO_BLOCK.put(Material.AMETHYST_SHARD, Material.AMETHYST_BLOCK);

        // --- Nether Gold ---
        ORE_TO_BLOCK.put(Material.GOLD_NUGGET, Material.GOLD_BLOCK);

        // --- Raw Ores (1.17+) ---
        // Converts 1 Raw Iron -> 1 Raw Iron Block
        ORE_TO_BLOCK.put(Material.RAW_IRON, Material.RAW_IRON_BLOCK);
        ORE_TO_BLOCK.put(Material.RAW_GOLD, Material.RAW_GOLD_BLOCK);
        ORE_TO_BLOCK.put(Material.RAW_COPPER, Material.RAW_COPPER_BLOCK);

        // --- Ingots (Fallback) ---
        ORE_TO_BLOCK.put(Material.IRON_INGOT, Material.IRON_BLOCK);
        ORE_TO_BLOCK.put(Material.GOLD_INGOT, Material.GOLD_BLOCK);
        ORE_TO_BLOCK.put(Material.COPPER_INGOT, Material.COPPER_BLOCK);
        ORE_TO_BLOCK.put(Material.NETHERITE_SCRAP, Material.NETHERITE_BLOCK);

        // --- Silk Touch Fallbacks ---
        // If the pickaxe has Silk Touch, it drops the Ore block itself.
        // We map these to their resource blocks to be safe.
        ORE_TO_BLOCK.put(Material.DIAMOND_ORE, Material.DIAMOND_BLOCK);
        ORE_TO_BLOCK.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND_BLOCK);
        ORE_TO_BLOCK.put(Material.EMERALD_ORE, Material.EMERALD_BLOCK);
        ORE_TO_BLOCK.put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD_BLOCK);
        ORE_TO_BLOCK.put(Material.COAL_ORE, Material.COAL_BLOCK);
        ORE_TO_BLOCK.put(Material.DEEPSLATE_COAL_ORE, Material.COAL_BLOCK);
    }

    public BlockAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleBlock(BlockBreakEvent event, Player player, Block block, ItemStack tool) {
        Collection<ItemStack> drops = block.getDrops(tool, player);

        Map<Material, Integer> dropCounts = new HashMap<>();
        for (ItemStack drop : drops) {
            dropCounts.merge(drop.getType(), drop.getAmount(), Integer::sum);
        }

        List<ItemStack> newDrops = new ArrayList<>();
        boolean converted = false;

        for (Map.Entry<Material, Integer> entry : dropCounts.entrySet()) {
            Material dropType = entry.getKey();
            int amount = entry.getValue();

            Material blockType = ORE_TO_BLOCK.get(dropType);

            // CHANGED: Removed "amount >= minDrops" check.
            // Now, if it's a mappable ore, it ALWAYS turns into a block.
            if (blockType != null) {
                // Give 1 block per stack of drops (OP Mode)
                newDrops.add(new ItemStack(blockType, 1));
                converted = true;
            } else {
                // No mapping found, drop original item
                newDrops.add(new ItemStack(dropType, amount));
            }
        }

        if (converted) {
            event.setDropItems(false); // Disable vanilla drops

            for (ItemStack drop : newDrops) {
                DropHelper.handleDrop(player, drop, block.getLocation());
            }
        }
    }
}