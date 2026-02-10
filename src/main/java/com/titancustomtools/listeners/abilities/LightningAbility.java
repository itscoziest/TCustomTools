package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.utils.DropHelper;
import com.titancustomtools.utils.WorldGuardHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class LightningAbility {

    private final TitanCustomTools plugin;
    private final Random random = new Random();

    public LightningAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleLightning(BlockBreakEvent event, Player player, Block centerBlock, ItemStack tool) {
        // Proc chance check
        double chance = plugin.getConfig().getDouble("lightning-pickaxe.proc-chance", 0.05);
        if (random.nextDouble() > chance) return;

        // Visual Effect
        centerBlock.getWorld().strikeLightningEffect(centerBlock.getLocation());

        List<String> allowedRegions = plugin.getConfig().getStringList("explosive-pickaxe.allowed-regions");

        int radius = 1; // 3x3x3 area
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    // IMPORTANT: Skip the center block.
                    // Let the main BlockBreakEvent (and TitanUtils) handle the center block
                    // to prevent double drops or conflicts.
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block b = centerBlock.getRelative(x, y, z);

                    if (b.getType() == Material.AIR || b.getType() == Material.BEDROCK) continue;

                    // Only break if inside allowed region (Safety)
                    if (WorldGuardHelper.isInMiningRegion(b.getLocation(), allowedRegions)) {

                        // Manually get drops and feed them into DropHelper for AutoPickup
                        for (ItemStack drop : b.getDrops(tool, player)) {
                            DropHelper.handleDrop(player, drop, b.getLocation());
                        }

                        b.setType(Material.AIR);
                        plugin.getStatsManager().incrementTotal(player, 1);
                    }
                }
            }
        }
    }
}