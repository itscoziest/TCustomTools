package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.utils.WorldGuardHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ExplosiveAbility {

    private final TitanCustomTools plugin;
    private final Random random;
    private final Set<Block> processingBlocks;

    public ExplosiveAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.processingBlocks = new HashSet<>();
    }

    public void handleExplosive(BlockBreakEvent event, Player player, Block block, ItemStack tool) {
        if (processingBlocks.contains(block)) return;

        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        block.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, block.getLocation().add(0.5, 0.5, 0.5), 1);

        // Debug mode check removed to prevent console spam
        List<String> allowedRegions = plugin.getConfig().getStringList("explosive-pickaxe.allowed-regions");

        List<Block> blocksAround = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block nearbyBlock = block.getRelative(x, y, z);

                    if (nearbyBlock.getType() == Material.AIR || !nearbyBlock.getType().isSolid()) continue;

                    Location checkLoc = nearbyBlock.getLocation().add(0.5, 0.5, 0.5);

                    if (!WorldGuardHelper.isInMiningRegion(checkLoc, allowedRegions)) {
                        // Logic remains, but the warning log is gone
                        continue;
                    }

                    blocksAround.add(nearbyBlock);
                }
            }
        }

        if (blocksAround.isEmpty()) {
            return;
        }

        int blocksToBreak = 5 + random.nextInt(4);
        Collections.shuffle(blocksAround);

        Location dropLocation = block.getLocation().add(0.5, 0.5, 0.5);

        int broken = 0;
        for (Block nearbyBlock : blocksAround) {
            if (broken >= blocksToBreak) break;

            processingBlocks.add(nearbyBlock);
            BlockBreakEvent breakEvent = new BlockBreakEvent(nearbyBlock, player);
            Bukkit.getPluginManager().callEvent(breakEvent);

            if (!breakEvent.isCancelled()) {
                Collection<ItemStack> drops = nearbyBlock.getDrops(tool, player);
                nearbyBlock.setType(Material.AIR);

                for (ItemStack drop : drops) {
                    nearbyBlock.getWorld().dropItemNaturally(dropLocation, drop);
                }
                broken++;
            }
            processingBlocks.remove(nearbyBlock);
        }
    }
}
