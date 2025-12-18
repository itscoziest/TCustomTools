package com.titancustomtools.managers;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final TitanCustomTools plugin;

    private final Map<UUID, Long> totalBlocksCache = new HashMap<>();

    private final Map<UUID, Long> rawBlocksCache = new HashMap<>();

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

        long currentRaw = getRawBlocks(player);
        rawBlocksCache.put(uuid, currentRaw + 1);

        incrementTotal(player, 1);
    }

    public void incrementTotal(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        long currentTotal = getTotalBlocks(player);
        totalBlocksCache.put(uuid, currentTotal + amount);
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
        ConfigurationSection sec = plugin.getConfig().createSection("stats");

        for (Map.Entry<UUID, Long> entry : totalBlocksCache.entrySet()) {
            sec.set(entry.getKey().toString() + ".total", entry.getValue());
        }

        for (Map.Entry<UUID, Long> entry : rawBlocksCache.entrySet()) {
            sec.set(entry.getKey().toString() + ".raw", entry.getValue());
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