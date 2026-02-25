package com.titancustomtools.api;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import org.bukkit.inventory.ItemStack;

public class TitanToolsAPI {

    /**
     * Checks if an item is a valid custom Titan Tool.
     */
    public static boolean isTitanTool(ItemStack item) {
        if (item == null) return false;
        return TitanCustomTools.getInstance().getToolManager().isCustomTool(item);
    }

    /**
     * Gets the specific ToolType of an item.
     * Returns null if it's not a custom tool.
     */
    public static ToolType getToolType(ItemStack item) {
        if (item == null) return null;
        return TitanCustomTools.getInstance().getToolManager().getToolType(item);
    }

    /**
     * Checks if an item matches a specific ToolType.
     * Use this in TitanFishing to check for specific rods (e.g. TITAN_ROD).
     */
    public static boolean isTool(ItemStack item, ToolType type) {
        ToolType found = getToolType(item);
        return found != null && found == type;
    }
}