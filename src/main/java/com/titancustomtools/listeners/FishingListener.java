package com.titancustomtools.listeners;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import com.titancustomtools.listeners.abilities.HellfireAbility;
import com.titancustomtools.listeners.abilities.SwiftcasterAbility;
import com.titancustomtools.managers.ToolManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class FishingListener implements Listener {

    private final TitanCustomTools plugin;
    private final ToolManager toolManager;
    private final SwiftcasterAbility swiftcasterAbility;
    private final HellfireAbility hellfireAbility;

    public FishingListener(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
        this.swiftcasterAbility = new SwiftcasterAbility(plugin);
        this.hellfireAbility = new HellfireAbility(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            item = player.getInventory().getItemInOffHand();
        }

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ToolType toolType = toolManager.getToolType(item);
        if (toolType == null) return;

        // --- SWIFTCASTER LOGIC ---
        if (toolType == ToolType.SWIFTCASTER) {
            swiftcasterAbility.handleFishing(event);
        }

        // --- HELLFIRE LOGIC ---
        if (toolType == ToolType.HELLFIRE) {
            // CAUGHT_FISH, REEL_IN, and IN_GROUND are all attempts to pull back
            if (event.getState() == PlayerFishEvent.State.REEL_IN ||
                    event.getState() == PlayerFishEvent.State.CAUGHT_FISH ||
                    event.getState() == PlayerFishEvent.State.IN_GROUND ||
                    event.getState() == PlayerFishEvent.State.FAILED_ATTEMPT) {

                hellfireAbility.handleReel(event);
            }
            else if (event.getState() == PlayerFishEvent.State.FISHING) {
                // Player cast the rod
                hellfireAbility.handleCast(event);
            }
        }
    }
}