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
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            FishHook hook = event.getHook();
            Player player = event.getPlayer();

            // Force wait time to be between 15 ticks (0.75s) and 60 ticks (3s).
            hook.setMinWaitTime(15);
            hook.setMaxWaitTime(60);

            // Disable vanilla Lure calculations
            hook.setApplyLure(false);

            // Subtle sound
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
        }
    }
}