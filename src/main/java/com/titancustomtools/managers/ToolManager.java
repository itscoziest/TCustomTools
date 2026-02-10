package com.titancustomtools.managers;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ToolManager {

    private final TitanCustomTools plugin;
    private final NamespacedKey toolTypeKey;
    private final NamespacedKey usesKey;
    private final NamespacedKey maxUsesKey;

    public ToolManager(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.toolTypeKey = new NamespacedKey(plugin, "tool_type");
        this.usesKey = new NamespacedKey(plugin, "current_uses");
        this.maxUsesKey = new NamespacedKey(plugin, "max_uses");
    }

    // Overloaded method for standard calls
    public ItemStack createTool(ToolType toolType) {
        return createTool(toolType, false, -1);
    }

    public ItemStack createTool(ToolType toolType, boolean isNetherite) {
        return createTool(toolType, isNetherite, -1);
    }

    public ItemStack createTool(ToolType toolType, boolean isNetherite, int uses) {
        // --- GOD / TITAN Logic ---
        if (toolType == ToolType.GOD) {
            ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&4&k;&r &c&lADMIN GOD PICKAXE &4&k;&r"));
                meta.setUnbreakable(true);
                meta.addEnchant(Enchantment.DIG_SPEED, 50, true);
                meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING, toolType.name());
                item.setItemMeta(meta);
            }
            return item;
        }

        if (toolType == ToolType.TITAN) {
            ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "Titan Pickaxe");
                meta.setUnbreakable(true);
                meta.addEnchant(Enchantment.DIG_SPEED, 6, true);
                meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING, toolType.name());
                item.setItemMeta(meta);
            }
            return item;
        }

        // --- STANDARD TOOL LOGIC ---
        String configPath = toolType.getConfigKey() + "-pickaxe";
        if (toolType == ToolType.LUMBERJACK) configPath = "lumberjack-axe";
        else if (toolType == ToolType.SWIFTCASTER) configPath = "swiftcaster-rod";
        else if (toolType == ToolType.HELLFIRE) configPath = "hellfire-rod";

        ConfigurationSection config = plugin.getConfig().getConfigurationSection(configPath);
        if (config != null && !config.getBoolean("enabled", true)) {
            return null;
        }

        Material material = getMaterialForTool(toolType, isNetherite);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return null;

        meta.setDisplayName(getDisplayName(toolType));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        if (config != null && config.contains("enchantments")) {
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

        // --- USES LOGIC ---
        if (uses > 0) {
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);
            meta.getPersistentDataContainer().set(maxUsesKey, PersistentDataType.INTEGER, uses);
            lore.add(" ");
            lore.add(ChatColor.GRAY + "Uses: " + ChatColor.GREEN + uses + "/" + uses);
        } else {
            // Permanent logic for rods
            if (toolType == ToolType.SWIFTCASTER || toolType == ToolType.HELLFIRE) {
                lore.add(" ");
                lore.add(ChatColor.GOLD + "Uses: " + ChatColor.LIGHT_PURPLE + "Permanent");
            }
        }

        meta.setLore(lore);
        meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING, toolType.name());
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Decrements the use of the tool in the player's main hand.
     * Returns TRUE if the tool broke, FALSE otherwise.
     */
    public boolean decrementUse(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();

        // If it doesn't have the key, it's permanent
        if (!meta.getPersistentDataContainer().has(usesKey, PersistentDataType.INTEGER)) {
            return false;
        }

        int current = meta.getPersistentDataContainer().get(usesKey, PersistentDataType.INTEGER);
        int max = meta.getPersistentDataContainer().getOrDefault(maxUsesKey, PersistentDataType.INTEGER, current);

        int newValue = current - 1;

        if (newValue <= 0) {
            // BROKE
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR)); // Remove item
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Your rod has run out of uses and broke!");
            return true;
        }

        // Update NBT
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, newValue);

        // Update Lore
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains("Uses:")) {
                    lore.set(i, ChatColor.GRAY + "Uses: " + ChatColor.GREEN + newValue + "/" + max);
                    break;
                }
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return false;
    }

    private Material getMaterialForTool(ToolType toolType, boolean isNetherite) {
        if (toolType == ToolType.LUMBERJACK) return isNetherite ? Material.NETHERITE_AXE : Material.DIAMOND_AXE;
        if (toolType == ToolType.SWIFTCASTER || toolType == ToolType.HELLFIRE) return Material.FISHING_ROD;
        return isNetherite ? Material.NETHERITE_PICKAXE : Material.DIAMOND_PICKAXE;
    }

    private String getDisplayName(ToolType toolType) {
        switch (toolType) {
            case SMELTER: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Smelter Pickaxe";
            case LUMBERJACK: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Lumberjack Axe";
            case EXPLOSIVE: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Explosive Pickaxe";
            case BLOCK: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Block Pickaxe";
            case BOUNTIFUL: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Bountiful Pickaxe";
            case SWIFTCASTER: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Swiftcaster Rod";
            case HELLFIRE: return ChatColor.RED + "" + ChatColor.ITALIC + "Hellfire Rod";
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
            case SWIFTCASTER: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Bites twice as fast!";
            case HELLFIRE: return ChatColor.GOLD + "" + ChatColor.ITALIC + "Can fish in Lava!";
            default: return "";
        }
    }

    public ToolType getToolType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        String typeStr = meta.getPersistentDataContainer().get(toolTypeKey, PersistentDataType.STRING);
        if (typeStr == null) return null;
        try { return ToolType.valueOf(typeStr); } catch (IllegalArgumentException e) { return null; }
    }

    // --- THIS IS THE MISSING METHOD CAUSING THE ERROR ---
    public boolean isCustomTool(ItemStack item) {
        return getToolType(item) != null;
    }

    public String getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&6TitanTools&8] &7"));
        String message = plugin.getConfig().getString("messages." + path, "");
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }
}