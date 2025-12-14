package com.titancustomtools.listeners;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class StatsListener implements Listener {

    private final TitanCustomTools plugin;

    public StatsListener(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.getStatsManager().incrementBlock(event.getPlayer());
    }
}