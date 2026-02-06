package com.titancustomtools.managers;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsManager {

    private final TitanCustomTools plugin;

    // FIXED: Use ConcurrentHashMap to prevent crashes during async saves
    private final Map<UUID, Long> totalBlocksCache = new ConcurrentHashMap<>();
    private final Map<UUID, Long> rawBlocksCache = new ConcurrentHashMap<>();

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

    public void incrementRaw(Player player) {
        UUID uuid = player.getUniqueId();
        rawBlocksCache.merge(uuid, 1L, Long::sum);
        incrementTotal(player, 1);
    }

    public void incrementTotal(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        totalBlocksCache.merge(uuid, (long) amount, Long::sum);
    }

    public long getTotalBlocks(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        return totalBlocksCache.getOrDefault(uuid, getRawBlocks(player));
    }

    public long getRawBlocks(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();

        if (!rawBlocksCache.containsKey(uuid)) {
            if (player.isOnline()) {
                long vanillaTotal = 0;
                Player p = player.getPlayer();

                if (p != null) {
                    for (Material mat : Material.values()) {
                        if (mat.isBlock()) {
                            try {
                                vanillaTotal += p.getStatistic(Statistic.MINE_BLOCK, mat);
                            } catch (IllegalArgumentException | NullPointerException ignored) {
                            }
                        }
                    }
                }
                rawBlocksCache.put(uuid, vanillaTotal);
            } else {
                return 0;
            }
        }
        return rawBlocksCache.get(uuid);
    }

    public long getBlocksMined(Player player) {
        return getTotalBlocks(player);
    }

    public void saveData() {
        // Create a copy to save safely
        Map<UUID, Long> totalCopy = new ConcurrentHashMap<>(totalBlocksCache);
        Map<UUID, Long> rawCopy = new ConcurrentHashMap<>(rawBlocksCache);

        // Run the config write synchronously to be safe with Bukkit API
        // or just ensure we don't access the live maps.
        // Since set() updates memory, we can do this part, but saveConfig writes to disk.
        // Ideally, we schedule the save back to main thread or keep it async if careful.
        // For safety, we will just set values here.

        try {
            ConfigurationSection sec = plugin.getConfig().createSection("stats");

            for (Map.Entry<UUID, Long> entry : totalCopy.entrySet()) {
                sec.set(entry.getKey().toString() + ".total", entry.getValue());
            }

            for (Map.Entry<UUID, Long> entry : rawCopy.entrySet()) {
                sec.set(entry.getKey().toString() + ".raw", entry.getValue());
            }

            // Note: saveConfig() writes to disk. It's usually okay async but better to be safe.
            plugin.saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        if (!plugin.getConfig().contains("stats")) return;
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("stats");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);

                if (sec.isConfigurationSection(key)) {
                    totalBlocksCache.put(uuid, sec.getLong(key + ".total"));
                    rawBlocksCache.put(uuid, sec.getLong(key + ".raw"));
                } else {
                    long oldVal = sec.getLong(key);
                    totalBlocksCache.put(uuid, oldVal);
                    rawBlocksCache.put(uuid, oldVal);
                }
            } catch (Exception e) {
            }
        }
    }

    public void shutdown() {
        saveData();
    }
}