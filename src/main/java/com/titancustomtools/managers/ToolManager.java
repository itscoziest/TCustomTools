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

    public ItemStack createTool(ToolType toolType, boolean isNetherite, int uses) {
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

        if (toolType == ToolType.REBIRTH) {
            ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "Rebirth Pickaxe");
                meta.setUnbreakable(true);
                meta.addEnchant(Enchantment.DIG_SPEED, 8, true);
                meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING, toolType.name());

                List<String> lore = new ArrayList<>();
                lore.add(getCustomMessage(toolType));

                if (uses > 0) {
                    meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);
                    meta.getPersistentDataContainer().set(maxUsesKey, PersistentDataType.INTEGER, uses);
                    lore.add(" ");
                    lore.add(ChatColor.GRAY + "Uses: " + ChatColor.GREEN + uses + "/" + uses);
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        }

        String configPath;
        if (isArmor(toolType)) {
            configPath = toolType.getConfigKey();
        } else if (isRod(toolType)) {
            configPath = toolType.getConfigKey().replace("_", "-");
            if (!configPath.endsWith("-rod")) {
                configPath += "-rod";
            }
        } else {
            configPath = toolType.getConfigKey() + "-pickaxe";
        }

        if (toolType == ToolType.LUMBERJACK) configPath = "lumberjack-axe";

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
        String msg = getCustomMessage(toolType);
        if (msg != null && !msg.isEmpty()) {
            lore.add(msg);
        }

        if (uses > 0) {
            meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);
            meta.getPersistentDataContainer().set(maxUsesKey, PersistentDataType.INTEGER, uses);
            lore.add(" ");
            lore.add(ChatColor.GRAY + "Uses: " + ChatColor.GREEN + uses + "/" + uses);
        } else {
            if (isRod(toolType) || isArmor(toolType)) {
                // Do nothing for armor uses display
            }
        }

        meta.setLore(lore);
        meta.getPersistentDataContainer().set(toolTypeKey, PersistentDataType.STRING, toolType.name());
        item.setItemMeta(meta);

        return item;
    }

    public boolean decrementUse(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();

        if (!meta.getPersistentDataContainer().has(usesKey, PersistentDataType.INTEGER)) {
            return false;
        }

        int current = meta.getPersistentDataContainer().get(usesKey, PersistentDataType.INTEGER);
        int max = meta.getPersistentDataContainer().getOrDefault(maxUsesKey, PersistentDataType.INTEGER, current);

        int newValue = current - 1;

        if (newValue <= 0) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Your tool has run out of uses and broke!");
            return true;
        }

        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, newValue);

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

    public boolean isRod(ToolType type) {
        return type.name().contains("ROD") || type == ToolType.SWIFTCASTER || type == ToolType.HELLFIRE;
    }

    public boolean isArmor(ToolType type) {
        return type.name().endsWith("_HELMET") || type.name().endsWith("_CHESTPLATE") ||
                type.name().endsWith("_LEGGINGS") || type.name().endsWith("_BOOTS");
    }

    private Material getMaterialForTool(ToolType toolType, boolean isNetherite) {
        if (isRod(toolType)) return Material.FISHING_ROD;
        if (toolType == ToolType.LUMBERJACK) return isNetherite ? Material.NETHERITE_AXE : Material.DIAMOND_AXE;

        switch (toolType) {
            case DIAMOND_HELMET: return Material.DIAMOND_HELMET;
            case DIAMOND_CHESTPLATE: return Material.DIAMOND_CHESTPLATE;
            case DIAMOND_LEGGINGS: return Material.DIAMOND_LEGGINGS;
            case DIAMOND_BOOTS: return Material.DIAMOND_BOOTS;
            case NETHERITE_HELMET: return Material.NETHERITE_HELMET;
            case NETHERITE_CHESTPLATE: return Material.NETHERITE_CHESTPLATE;
            case NETHERITE_LEGGINGS: return Material.NETHERITE_LEGGINGS;
            case NETHERITE_BOOTS: return Material.NETHERITE_BOOTS;
            default: return isNetherite ? Material.NETHERITE_PICKAXE : Material.DIAMOND_PICKAXE;
        }
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
            case TITAN: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Titan Pickaxe";
            case LIGHTNING: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Lightning Pickaxe";
            case KEYFINDER: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Keyfinder Pickaxe";
            case CURRENCY: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Currency Pickaxe";
            case NITRO: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Nitro Pickaxe";
            case REBIRTH: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Rebirth Pickaxe";
            case TITAN_ROD: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Titan Rod";
            case TICKET_ROD: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Ticket Rod";
            case TOKEN_ROD: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Token Rod";
            case CRATE_ROD: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Crate Rod";
            case LIGHTNING_ROD: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Lightning Rod";

            case DIAMOND_HELMET: return ChatColor.AQUA + "" + ChatColor.BOLD + "Titan Helmet";
            case DIAMOND_CHESTPLATE: return ChatColor.AQUA + "" + ChatColor.BOLD + "Titan Chestplate";
            case DIAMOND_LEGGINGS: return ChatColor.AQUA + "" + ChatColor.BOLD + "Titan Leggings";
            case DIAMOND_BOOTS: return ChatColor.AQUA + "" + ChatColor.BOLD + "Titan Boots";

            case NETHERITE_HELMET: return ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Titan Netherite Helmet";
            case NETHERITE_CHESTPLATE: return ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Titan Netherite Chestplate";
            case NETHERITE_LEGGINGS: return ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Titan Netherite Leggings";
            case NETHERITE_BOOTS: return ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Titan Netherite Boots";

            default: return ChatColor.AQUA + "" + ChatColor.ITALIC + "Custom Tool";
        }
    }

    private String getCustomMessage(ToolType toolType) {
        if (isArmor(toolType)) {
            return ChatColor.GOLD + "Full Set Bonus: " + ChatColor.YELLOW + "Speed III & Strength II";
        }

        switch (toolType) {
            case SMELTER: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "A trusty pick that smelts what you mine!";
            case LUMBERJACK: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Chop down entire trees!";
            case EXPLOSIVE: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Blast your way to the top!";
            case BLOCK: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Convert blocks instantly!";
            case BOUNTIFUL: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Drops the ores from others nearby!";
            case SWIFTCASTER: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Bites twice as fast!";
            case HELLFIRE: return ChatColor.GOLD + "" + ChatColor.ITALIC + "Can fish in Lava!";
            case LIGHTNING: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Chance to strike lightning and break 3x3 radius!";
            case KEYFINDER: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "A pickaxe that has low chance to give you random keys!";
            case CURRENCY: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Low chance to activate a currency rain!";
            case NITRO: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Gives you a medium chance of activating haste 3 and speed 3!";
            case REBIRTH: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Explosive & Bountiful combined!";
            case TITAN_ROD: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Double chance to get titan point while fishing!";
            case TICKET_ROD: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Chance to fish randomly 10-50 tickets!";
            case TOKEN_ROD: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Chance to fish randomly 10-50 tokens!";
            case CRATE_ROD: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Double chance to obtain fish crate key!";
            case LIGHTNING_ROD: return ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Chance to strike when you bite and catch something!";
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

    public boolean isCustomTool(ItemStack item) {
        return getToolType(item) != null;
    }

    public String getMessage(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&8[&6TitanTools&8] &7"));
        String message = plugin.getConfig().getString("messages." + path, "");
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }
}