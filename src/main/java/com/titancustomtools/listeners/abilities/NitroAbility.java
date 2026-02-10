package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Random;

public class NitroAbility {
    private final TitanCustomTools plugin;
    private final Random random = new Random();

    public NitroAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleNitro(Player player) {
        double chance = plugin.getConfig().getDouble("nitro-pickaxe.proc-chance", 0.03);

        if (random.nextDouble() < chance) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 100, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));

            // Removed symbols
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lNITRO ACTIVATED!"));
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 2f);
        }
    }
}