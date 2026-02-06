package com.titancustomtools.managers;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
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

    public ItemStack createTool(ToolType toolType) {
        return createTool(toolType, false);
    }

    public ItemStack createTool(ToolType toolType, boolean isNetherite) {
        // --- SPECIAL HANDLING FOR GOD PICKAXE ---
        if (toolType == ToolType.GOD) {
            ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4&k;&r &c&lADMIN GOD PICKAXE &4&k;&r"));
                meta.setUnbreakable(true);
                meta.addEnchant(Enchantment.DIG_SPEED, 50, true);
                meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 10, true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "The ultimate power for admins.");
                lore.add(ChatColor.GRAY + "Lightning | Explosive | Auto-Smelt");
                meta.setLore(lore);

                meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING, toolType.name());
                item.setItemMeta(meta);
            }
            return item;
        }

        // --- SPECIAL HANDLING FOR TITAN PICKAXE (CLIENT REQUEST) ---
        if (toolType == ToolType.TITAN) {
            ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // THEME FIX: Aqua + Italic (Matches other tools)
                meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "Titan Pickaxe");

                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

                // Enchants: Efficiency 6, Fortune 4
                meta.addEnchant(Enchantment.DIG_SPEED, 6, true);
                meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 4, true);

                // THEME FIX: Dark Purple + Italic (Matches other tools)
                // Removed headers and bullet points to avoid broken symbols
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Explosive + Bountiful");
                lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "You won't lose this on death");
                lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "You won't lose this on prestige or rebirth");

                meta.setLore(lore);

                meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING, toolType.name());
                item.setItemMeta(meta);
            }
            return item;
        }

        // --- STANDARD LOGIC FOR OTHER TOOLS ---
        String configPath = toolType.getConfigKey() + "-pickaxe";
        if (toolType == ToolType.LUMBERJACK) {
            configPath = "lumberjack-axe";
        }

        ConfigurationSection config = plugin.getConfig().getConfigurationSection(configPath);
        if (config == null || !config.getBoolean("enabled", true)) {
            return null;
        }

        Material material = getMaterialForTool(toolType, isNetherite);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return null;

        meta.setDisplayName(getDisplayName(toolType));
        meta.setUnbreakable(true);

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

        meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING, toolType.name());
        item.setItemMeta(meta);

        return item;
    }

    private Material getMaterialForTool(ToolType toolType, boolean isNetherite) {
        if (toolType == ToolType.LUMBERJACK) {
            return isNetherite ? Material.NETHERITE_AXE : Material.DIAMOND_AXE;
        }
        return isNetherite ? Material.NETHERITE_PICKAXE : Material.DIAMOND_PICKAXE;
    }

    private String getDisplayName(ToolType toolType) {
        switch (toolType) {
            case SMELTER: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Smelter Pickaxe";
            case LUMBERJACK: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Lumberjack Axe";
            case EXPLOSIVE: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Explosive Pickaxe";
            case BLOCK: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Block Pickaxe";
            case BOUNTIFUL: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Bountiful Pickaxe";
            default: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Custom Tool";
        }
    }

    private String getCustomMessage(ToolType toolType) {
        switch (toolType) {
            case SMELTER: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "A trusty pick that smelts what you mine!";
            case LUMBERJACK: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Chop down entire trees!";
            case EXPLOSIVE: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Blast your way to the top!";
            case BLOCK: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Convert blocks instantly!";
            case BOUNTIFUL: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Drops the ores from others nearby!";
            default: return "";
        }
    }

    public ToolType getToolType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String typeStr = meta.getPersistentDataContainer().get(toolTypeKey, PersistentDataType.STRING);
        if (typeStr == null) return null;
        try { return ToolType.valueOf(typeStr); } catch (IllegalArgumentException e) { return null; }
    }

    public boolean isCustomTool(ItemStack item) {
        return getToolType(item) != null;
    }

    public String getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&6TitanTools&8] &7"));
        String message = plugin.getConfig().getString("messages." + path, "");
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }

    public NamespacedKey getToolTypeKey() {
        return toolTypeKey;
    }
}