package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;

public class SwiftcasterAbility {

    private final TitanCustomTools plugin;

    public SwiftcasterAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleFishing(PlayerFishEvent event) {
        // This state triggers when the bobber is cast into the water
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            FishHook hook = event.getHook();
            Player player = event.getPlayer();

            // Vanilla logic: ~100 to 600 ticks (5s - 30s)
            // Lure III removes ~150 ticks (7.5s) from max wait.

            // New "Swiftcaster" Logic:
            // Force wait time to be between 15 ticks (0.75s) and 60 ticks (3s).
            // This is drastically faster than even a maxed vanilla rod.
            hook.setMinWaitTime(15);
            hook.setMaxWaitTime(60);

            // Disable vanilla Lure calculations so our custom values aren't overwritten/broken
            hook.setApplyLure(false);

            // Play a subtle sound so the player knows the "Swift" effect is active
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
        }
    }
}