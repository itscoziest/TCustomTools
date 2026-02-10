package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.Random;

public class KeyfinderAbility {
    private final TitanCustomTools plugin;
    private final Random random = new Random();

    public KeyfinderAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleKeyfinder(Player player) {
        double roll = random.nextDouble();

        // DRASTICALLY REDUCED RATES
        // Titan: 0.005% (was 0.01%)
        double titanChance = plugin.getConfig().getDouble("keyfinder-pickaxe.titan-key-chance", 0.00002);
        // Normal: 0.05% (was 0.1%)
        double normalKeyChance = plugin.getConfig().getDouble("keyfinder-pickaxe.key-chance", 0.0002);

        if (roll <= titanChance) {
            giveKey(player, "titan");
        } else if (roll <= (titanChance + normalKeyChance)) {
            String[] keys = {"fish", "rebirth", "koth", "vote"};
            giveKey(player, keys[random.nextInt(keys.length)]);
        }
    }

    private void giveKey(Player player, String type) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate key give " + player.getName() + " " + type + " 1");
        // Removed symbol, kept it simple
        player.sendMessage(ChatColor.GOLD + "You found a " + ChatColor.YELLOW + type.toUpperCase() + ChatColor.GOLD + " key while mining!");
    }
}