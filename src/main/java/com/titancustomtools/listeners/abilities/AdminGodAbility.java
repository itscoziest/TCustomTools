package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.utils.DropHelper;
import com.titancustomtools.utils.WorldGuardHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AdminGodAbility {

    private final TitanCustomTools plugin;
    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();
    private static final Map<Material, Material> ORE_TO_BLOCK = new HashMap<>();

    static {
        // Mappings for Smelt + Block Logic
        SMELT_MAP.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.NETHER_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
        SMELT_MAP.put(Material.RAW_IRON, Material.IRON_INGOT);
        SMELT_MAP.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.RAW_COPPER, Material.COPPER_INGOT);

        ORE_TO_BLOCK.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
        ORE_TO_BLOCK.put(Material.EMERALD, Material.EMERALD_BLOCK);
        ORE_TO_BLOCK.put(Material.COAL, Material.COAL_BLOCK);
        ORE_TO_BLOCK.put(Material.REDSTONE, Material.REDSTONE_BLOCK);
        ORE_TO_BLOCK.put(Material.LAPIS_LAZULI, Material.LAPIS_BLOCK);
        ORE_TO_BLOCK.put(Material.QUARTZ, Material.QUARTZ_BLOCK);
        ORE_TO_BLOCK.put(Material.IRON_INGOT, Material.IRON_BLOCK);
        ORE_TO_BLOCK.put(Material.GOLD_INGOT, Material.GOLD_BLOCK);
        ORE_TO_BLOCK.put(Material.COPPER_INGOT, Material.COPPER_BLOCK);
        ORE_TO_BLOCK.put(Material.NETHERITE_SCRAP, Material.NETHERITE_BLOCK);
    }

    public AdminGodAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleGodMode(BlockBreakEvent event, Player player, Block centerBlock, ItemStack tool) {
        // 1. SAFETY: Check Mining Region First
        List<String> allowedRegions = plugin.getConfig().getStringList("explosive-pickaxe.allowed-regions");

        if (!WorldGuardHelper.isInMiningRegion(centerBlock.getLocation(), allowedRegions)) {
            player.sendMessage(ChatColor.RED + "God Pickaxe powers are restricted to Mining Regions!");
            return;
        }

        // 2. TRIGGER THE NEW "COOLER" EFFECTS
        playGodEffects(centerBlock.getLocation().add(0.5, 0.0, 0.5), player);

        // 3. LOGIC: Handle Drops & Explosion
        event.setDropItems(false);

        // Collect blocks to break (Radius 2 = 5x5x5)
        List<Block> blocksToProcess = new ArrayList<>();
        blocksToProcess.add(centerBlock);

        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    Block neighbor = centerBlock.getRelative(x, y, z);

                    if (neighbor.getType() == Material.AIR || neighbor.getType() == Material.BEDROCK) continue;

                    // Double check region for every block to be safe
                    if (WorldGuardHelper.isInMiningRegion(neighbor.getLocation(), allowedRegions)) {
                        blocksToProcess.add(neighbor);
                    }
                }
            }
        }

        Location dropLoc = centerBlock.getLocation().add(0.5, 0.5, 0.5);

        for (Block b : blocksToProcess) {
            // Get original drops
            Collection<ItemStack> originalDrops = b.getDrops(tool, player);

            for (ItemStack drop : originalDrops) {
                // Apply "Bountiful" (Double drops)
                drop.setAmount(drop.getAmount() * 2);

                // Convert (Smelt -> Block)
                ItemStack finalItem = processItem(drop);
                DropHelper.handleDrop(player, finalItem, dropLoc);
            }

            // --- CHAOS PARTICLES ---
            // Keep the simple explosion at every block for chaos
            b.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, b.getLocation().add(0.5, 0.5, 0.5), 1);

            // Set to Air (Explosive effect)
            if (!b.equals(centerBlock)) {
                b.setType(Material.AIR);
                plugin.getStatsManager().incrementTotal(player, 1);
            }
        }
    }

    // --- NEW METHOD FOR ADVANCED EFFECTS ---
    private void playGodEffects(Location center, Player player) {
        World world = center.getWorld();

        // 1. Divine Sound Theme (Layered Sounds)
        // Deep impact boom
        world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 0.7f);
        // Ominous background roar
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.5f);
        // High-pitch magical chime
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 2.0f);

        // 2. Visual Impact
        world.strikeLightningEffect(center);

        // Make the player flash with power briefly
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 0, false, false, false));

        // 3. Animated Particle Spiral
        new BukkitRunnable() {
            double angle = 0;
            double y = 0;

            @Override
            public void run() {
                // Run animation for 3 blocks high then stop
                if (y > 3) {
                    this.cancel();
                    return;
                }

                // Create a double helix rising
                for (int i = 0; i < 2; i++) {
                    double currentAngle = angle + (i * Math.PI);
                    double x = 1.5 * Math.cos(currentAngle); // Radius 1.5
                    double z = 1.5 * Math.sin(currentAngle);

                    Location particleLoc = center.clone().add(x, y, z);

                    // Soul Fire for a mystical blue flame look
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 0, 0, 0, 0);
                    // End Rod for bright white sparkles
                    world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                }

                y += 0.2; // Move up
                angle += 0.4; // Rotate
            }
        }.runTaskTimer(plugin, 0, 1); // Run every tick for smooth animation
    }

    private ItemStack processItem(ItemStack input) {
        Material type = input.getType();
        int amount = input.getAmount();

        // Smelt Logic
        if (SMELT_MAP.containsKey(type)) {
            type = SMELT_MAP.get(type);
        }

        // Block Logic (1:1 conversion for God Mode)
        if (ORE_TO_BLOCK.containsKey(type)) {
            type = ORE_TO_BLOCK.get(type);
        }

        return new ItemStack(type, amount);
    }
}