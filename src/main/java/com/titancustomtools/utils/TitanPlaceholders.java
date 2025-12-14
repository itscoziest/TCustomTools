package com.titancustomtools.utils;

import com.titancustomtools.TitanCustomTools;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TitanPlaceholders extends PlaceholderExpansion {

    private final TitanCustomTools plugin;

    public TitanPlaceholders(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "titancustomtools";
    }

    @Override
    public @NotNull String getAuthor() {
        return "StrikesDev";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equalsIgnoreCase("rawblocks") ||
                identifier.equalsIgnoreCase("totalblocks") ||
                identifier.equalsIgnoreCase("totalblocks_mined")) {

            long count = plugin.getStatsManager().getBlocksMined(player);
            return String.valueOf(count);
        }

        return null;
    }
}