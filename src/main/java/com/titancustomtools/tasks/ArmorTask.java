package com.titancustomtools.tasks;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import com.titancustomtools.managers.ToolManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ArmorTask extends BukkitRunnable {

    private final ToolManager toolManager;

    public ArmorTask(TitanCustomTools plugin) {
        this.toolManager = plugin.getToolManager();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkAndApplyEffects(player);
        }
    }

    private void checkAndApplyEffects(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        if (helmet == null || chest == null || legs == null || boots == null) {
            return;
        }

        ToolType hType = toolManager.getToolType(helmet);
        ToolType cType = toolManager.getToolType(chest);
        ToolType lType = toolManager.getToolType(legs);
        ToolType bType = toolManager.getToolType(boots);

        if (hType == null || cType == null || lType == null || bType == null) {
            return;
        }

        // Check for full Diamond Set
        boolean isDiamondSet = (hType == ToolType.DIAMOND_HELMET && cType == ToolType.DIAMOND_CHESTPLATE &&
                lType == ToolType.DIAMOND_LEGGINGS && bType == ToolType.DIAMOND_BOOTS);

        // Check for full Netherite Set
        boolean isNetheriteSet = (hType == ToolType.NETHERITE_HELMET && cType == ToolType.NETHERITE_CHESTPLATE &&
                lType == ToolType.NETHERITE_LEGGINGS && bType == ToolType.NETHERITE_BOOTS);

        if (isDiamondSet || isNetheriteSet) {
            // Amplifier 2 = Speed 3
            // Amplifier 1 = Strength 2
            // Duration 40 ticks = 2 seconds (refreshed every 1 second)
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 40, 1, false, false, true));
        }
    }
}