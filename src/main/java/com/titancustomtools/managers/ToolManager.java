package com.titancustomtools.managers;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ToolManager {

    private final TitanCustomTools plugin;
    private final NamespacedKey toolTypeKey;

    public ToolManager(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.toolTypeKey = new NamespacedKey(plugin, "tool_type");
    }

    // Overload for backward compatibility (plugins/code calling this without args get Diamond)
    public ItemStack createTool(ToolType toolType) {
        return createTool(toolType, false);
    }

    public ItemStack createTool(ToolType toolType, boolean isNetherite) {
        String configPath = toolType.getConfigKey() + "-pickaxe";
        if (toolType == ToolType.LUMBERJACK) {
            configPath = "lumberjack-axe";
        }

        ConfigurationSection config = plugin.getConfig().getConfigurationSection(configPath);
        if (config == null || !config.getBoolean("enabled", true)) {
            return null;
        }

        // 1. DETERMINE MATERIAL
        Material material = getMaterialForTool(toolType, isNetherite);

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return null;

        meta.setDisplayName(getDisplayName(toolType));

        // 2. MAKE UNBREAKABLE
        // This ensures the Netherite tool (and Diamond) will never break.
        meta.setUnbreakable(true);

        // 3. APPLY ENCHANTS FROM CONFIG
        // This pulls Efficiency 5, Fortune 3, etc. from your existing config.yml
        if (config.contains("enchantments")) {
            for (String enchantStr : config.getStringList("enchantments")) {
                String[] parts = enchantStr.split(":");
                if (parts.length == 2) {
                    Enchantment enchant = Enchantment.getByName(parts[0]);
                    if (enchant != null) {
                        int level = Integer.parseInt(parts[1]);
                        meta.addEnchant(enchant, level, true);
                    }
                }
            }
        }

        List<String> lore = new ArrayList<>();
        lore.add(getCustomMessage(toolType));
        meta.setLore(lore);

        // Add Persistent Data so other plugins/listeners recognize this tool
        meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING,
                toolType.name());

        item.setItemMeta(meta);

        return item;
    }

    private Material getMaterialForTool(ToolType toolType, boolean isNetherite) {
        if (toolType == ToolType.LUMBERJACK) {
            // Lumberjack is an Axe
            return isNetherite ? Material.NETHERITE_AXE : Material.DIAMOND_AXE;
        }
        // All others are Pickaxes
        return isNetherite ? Material.NETHERITE_PICKAXE : Material.DIAMOND_PICKAXE;
    }

    private String getDisplayName(ToolType toolType) {
        switch (toolType) {
            case SMELTER:
                return ChatColor.AQUA + "" + ChatColor.ITALIC + "Smelter Pickaxe";
            case LUMBERJACK:
                return ChatColor.AQUA + "" + ChatColor.ITALIC + "Lumberjack Axe";
            case EXPLOSIVE:
                return ChatColor.AQUA + "" + ChatColor.ITALIC + "Explosive Pickaxe";
            case BLOCK:
                return ChatColor.AQUA + "" + ChatColor.ITALIC + "Block Pickaxe";
            case BOUNTIFUL:
                return ChatColor.AQUA + "" + ChatColor.ITALIC + "Bountiful Pickaxe";
            default:
                return ChatColor.AQUA + "" + ChatColor.ITALIC + "Custom Tool";
        }
    }

    private String getCustomMessage(ToolType toolType) {
        switch (toolType) {
            case SMELTER:
                return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "A trusty pick that smelts what you mine!";
            case LUMBERJACK:
                return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Chop down entire trees!";
            case EXPLOSIVE:
                return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Blast your way to the top!";
            case BLOCK:
                return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Convert blocks instantly!";
            case BOUNTIFUL:
                return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Drops the ores from others nearby!";
            default:
                return "";
        }
    }

    public ToolType getToolType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        String typeStr = meta.getPersistentDataContainer().get(toolTypeKey, PersistentDataType.STRING);
        if (typeStr == null) return null;

        try {
            return ToolType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isCustomTool(ItemStack item) {
        return getToolType(item) != null;
    }

    public String getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.prefix", "&8[&6TitanTools&8] &7"));
        String message = plugin.getConfig().getString("messages." + path, "");
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }

    public NamespacedKey getToolTypeKey() {
        return toolTypeKey;
    }
}