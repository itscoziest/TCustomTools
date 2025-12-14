package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.utils.DropHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SmelterAbility {

    private final TitanCustomTools plugin;
    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();

    static {
        SMELT_MAP.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        SMELT_MAP.put(Material.COBBLESTONE, Material.STONE);
        SMELT_MAP.put(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE);
        SMELT_MAP.put(Material.STONE, Material.SMOOTH_STONE);
        SMELT_MAP.put(Material.SAND, Material.GLASS);
        SMELT_MAP.put(Material.RED_SAND, Material.GLASS);
        SMELT_MAP.put(Material.CLAY, Material.TERRACOTTA);
        SMELT_MAP.put(Material.NETHERRACK, Material.NETHER_BRICK);
        SMELT_MAP.put(Material.CLAY_BALL, Material.BRICK);
        SMELT_MAP.put(Material.RAW_IRON, Material.IRON_INGOT);
        SMELT_MAP.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.CACTUS, Material.GREEN_DYE);
        SMELT_MAP.put(Material.SEA_PICKLE, Material.LIME_DYE);
        SMELT_MAP.put(Material.CHORUS_FRUIT, Material.POPPED_CHORUS_FRUIT);
        SMELT_MAP.put(Material.WET_SPONGE, Material.SPONGE);
        SMELT_MAP.put(Material.OAK_LOG, Material.CHARCOAL);
        SMELT_MAP.put(Material.SPRUCE_LOG, Material.CHARCOAL);
        SMELT_MAP.put(Material.BIRCH_LOG, Material.CHARCOAL);
        SMELT_MAP.put(Material.JUNGLE_LOG, Material.CHARCOAL);
        SMELT_MAP.put(Material.ACACIA_LOG, Material.CHARCOAL);
        SMELT_MAP.put(Material.DARK_OAK_LOG, Material.CHARCOAL);
        SMELT_MAP.put(Material.MANGROVE_LOG, Material.CHARCOAL);
        SMELT_MAP.put(Material.CHERRY_LOG, Material.CHARCOAL);
        SMELT_MAP.put(Material.CRIMSON_STEM, Material.CHARCOAL);
        SMELT_MAP.put(Material.WARPED_STEM, Material.CHARCOAL);
    }

    public SmelterAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleSmelter(BlockBreakEvent event, Player player, Block block, ItemStack tool) {
        Material blockType = block.getType();
        Material smeltedType = SMELT_MAP.get(blockType);

        if (smeltedType == null) {
            return;
        }

        event.setDropItems(false);

        Collection<ItemStack> drops = block.getDrops(tool, player);

        for (ItemStack drop : drops) {
            ItemStack smelted = new ItemStack(smeltedType, drop.getAmount());
            DropHelper.handleDrop(player, smelted, block.getLocation());
        }
    }
}