package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Random;

public class CurrencyAbility {
    private final TitanCustomTools plugin;
    private final Random random = new Random();

    public CurrencyAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleBlockBreak(Player player) {
        if (player.hasMetadata("currency_rain")) {
            giveRandomCurrency(player);
            return;
        }

        double chance = plugin.getConfig().getDouble("currency-pickaxe.activation-chance", 0.005);
        if (random.nextDouble() < chance) {
            startRain(player);
        }
    }

    private void startRain(Player player) {
        player.setMetadata("currency_rain", new FixedMetadataValue(plugin, true));
        // Removed symbol
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "CURRENCY RAIN ACTIVATED! Mine fast for 10 seconds!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        int duration = plugin.getConfig().getInt("currency-pickaxe.rain-duration", 10) * 20;

        new BukkitRunnable() {
            @Override
            public void run() {
                player.removeMetadata("currency_rain", plugin);
                player.sendMessage(ChatColor.RED + "The currency rain has ended!");
            }
        }.runTaskLater(plugin, duration);
    }

    private void giveRandomCurrency(Player player) {
        double roll = random.nextDouble();
        if (roll < 0.15) { // 15% Money
            int amount = 5000 + random.nextInt(45001);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + amount);
        } else if (roll < 0.25) { // 10% Tokens
            int amount = 5 + random.nextInt(26);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tokens give " + player.getName() + " " + amount);
        } else if (roll < 0.35) { // 10% Tickets
            int amount = 5 + random.nextInt(26);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tickets give " + player.getName() + " " + amount);
        }
    }
}