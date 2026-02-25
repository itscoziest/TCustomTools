package com.titancustomtools.enums;

public enum ToolType {
    // Pickaxes
    SMELTER("Smelter Pickaxe", "smelter"),
    LUMBERJACK("Lumberjack Axe", "lumberjack"),
    EXPLOSIVE("Explosive Pickaxe", "explosive"),
    BLOCK("Block Pickaxe", "block"),
    BOUNTIFUL("Bountiful Pickaxe", "bountiful"),
    TITAN("Titan Pickaxe", "titan"),
    GOD("God Pickaxe", "god"),
    LIGHTNING("Lightning Pickaxe", "lightning"),
    KEYFINDER("Keyfinder Pickaxe", "keyfinder"),
    CURRENCY("Currency Pickaxe", "currency"),
    NITRO("Nitro Pickaxe", "nitro"),
    REBIRTH("Rebirth Pickaxe", "rebirth"),

    // Rods
    SWIFTCASTER("Swiftcaster Rod", "swiftcaster"),
    HELLFIRE("Hellfire Rod", "hellfire"),
    TITAN_ROD("Titan Rod", "titan_rod"),
    TICKET_ROD("Ticket Rod", "ticket_rod"),
    TOKEN_ROD("Token Rod", "token_rod"),
    CRATE_ROD("Crate Rod", "crate_rod"),
    LIGHTNING_ROD("Lightning Rod", "lightning_rod"),

    // Armor - Diamond
    DIAMOND_HELMET("Titan Diamond Helmet", "diamond-helmet"),
    DIAMOND_CHESTPLATE("Titan Diamond Chestplate", "diamond-chestplate"),
    DIAMOND_LEGGINGS("Titan Diamond Leggings", "diamond-leggings"),
    DIAMOND_BOOTS("Titan Diamond Boots", "diamond-boots"),

    // Armor - Netherite
    NETHERITE_HELMET("Titan Netherite Helmet", "netherite-helmet"),
    NETHERITE_CHESTPLATE("Titan Netherite Chestplate", "netherite-chestplate"),
    NETHERITE_LEGGINGS("Titan Netherite Leggings", "netherite-leggings"),
    NETHERITE_BOOTS("Titan Netherite Boots", "netherite-boots");

    private final String displayName;
    private final String configKey;

    ToolType(String displayName, String configKey) {
        this.displayName = displayName;
        this.configKey = configKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public static ToolType fromString(String str) {
        for (ToolType type : values()) {
            if (type.getConfigKey().equalsIgnoreCase(str) || type.name().equalsIgnoreCase(str)) {
                return type;
            }
        }
        return null;
    }
}