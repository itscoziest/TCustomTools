package com.titancustomtools.listeners;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import com.titancustomtools.listeners.abilities.*;
import com.titancustomtools.managers.ToolManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ToolListener implements Listener {

    private final TitanCustomTools plugin;
    private final ToolManager toolManager;
    private final SmelterAbility smelterAbility;
    private final LumberjackAbility lumberjackAbility;
    private final ExplosiveAbility explosiveAbility;
    private final BlockAbility blockAbility;
    private final BountifulAbility bountifulAbility;
    private final AdminGodAbility adminGodAbility;

    // New Abilities
    private final LightningAbility lightningAbility;
    private final KeyfinderAbility keyfinderAbility;
    private final CurrencyAbility currencyAbility;
    private final NitroAbility nitroAbility;

    public ToolListener(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
        this.smelterAbility = new SmelterAbility(plugin);
        this.lumberjackAbility = new LumberjackAbility(plugin);
        this.explosiveAbility = new ExplosiveAbility(plugin);
        this.blockAbility = new BlockAbility(plugin);
        this.bountifulAbility = new BountifulAbility(plugin);
        this.adminGodAbility = new AdminGodAbility(plugin);
        this.lightningAbility = new LightningAbility(plugin);
        this.keyfinderAbility = new KeyfinderAbility(plugin);
        this.currencyAbility = new CurrencyAbility(plugin);
        this.nitroAbility = new NitroAbility(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) return;

        ToolType toolType = toolManager.getToolType(item);
        if (toolType == null) return;

        Block block = event.getBlock();

        switch (toolType) {
            case LIGHTNING:
                lightningAbility.handleLightning(event, player, block, item);
                break;
            case KEYFINDER:
                keyfinderAbility.handleKeyfinder(player);
                break;
            case CURRENCY:
                currencyAbility.handleBlockBreak(player);
                break;
            case NITRO:
                nitroAbility.handleNitro(player);
                break;
            case REBIRTH:
                bountifulAbility.handleBountiful(event, player, block, item);
                explosiveAbility.handleExplosive(event, player, block, item);
                break;
            case SMELTER:
                smelterAbility.handleSmelter(event, player, block, item);
                break;
            case LUMBERJACK:
                lumberjackAbility.handleLumberjack(event, player, block, item);
                break;
            case EXPLOSIVE:
                explosiveAbility.handleExplosive(event, player, block, item);
                break;
            case BLOCK:
                blockAbility.handleBlock(event, player, block, item);
                break;
            case BOUNTIFUL:
                bountifulAbility.handleBountiful(event, player, block, item);
                break;
            case TITAN:
                bountifulAbility.handleBountiful(event, player, block, item);
                explosiveAbility.handleExplosive(event, player, block, item);
                break;
            case GOD:
                adminGodAbility.handleGodMode(event, player, block, item);
                break;
        }
    }
}