package com.titancustomtools.managers;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final TitanCustomTools plugin;
    private final Map<UUID, Long> blockCache = new HashMap<>();

    public StatsManager(TitanCustomTools plugin) {
        this.plugin = plugin;
        loadData();

        new BukkitRunnable() {
            @Override
            public void run() {
                saveData();
            }
        }.runTaskTimerAsynchronously(plugin, 6000L, 6000L);
    }

    public void incrementBlock(Player player) {
        UUID uuid = player.getUniqueId();
        blockCache.put(uuid, getBlocksMined(player) + 1);
    }

    public long getBlocksMined(Player player) {
        UUID uuid = player.getUniqueId();

        if (!blockCache.containsKey(uuid)) {
            if (player.isOnline()) {
                importVanillaStats(player);
            } else {
                return 0;
            }
        }
        return blockCache.getOrDefault(uuid, 0L);
    }

    private void importVanillaStats(Player player) {
        long total = 0;
        for (Material mat : Material.values()) {
            if (mat.isBlock()) {
                try {
                    total += player.getStatistic(Statistic.MINE_BLOCK, mat);
                } catch (IllegalArgumentException | NullPointerException ignored) {
                }
            }
        }
        blockCache.put(player.getUniqueId(), total);
    }

    public void saveData() {
        ConfigurationSection sec = plugin.getConfig().createSection("stats");
        for (Map.Entry<UUID, Long> entry : blockCache.entrySet()) {
            sec.set(entry.getKey().toString(), entry.getValue());
        }
        plugin.saveConfig();
    }

    private void loadData() {
        if (!plugin.getConfig().contains("stats")) return;
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("stats");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long amount = sec.getLong(key);
                blockCache.put(uuid, amount);
            } catch (Exception e) {
            }
        }
    }

    public void shutdown() {
        saveData();
    }
}