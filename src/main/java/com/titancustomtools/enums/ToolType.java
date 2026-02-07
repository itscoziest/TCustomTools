package com.titancustomtools.enums;

public enum ToolType {
    SMELTER("Smelter Pickaxe", "smelter"),
    LUMBERJACK("Lumberjack Axe", "lumberjack"),
    EXPLOSIVE("Explosive Pickaxe", "explosive"),
    BLOCK("Block Pickaxe", "block"),
    BOUNTIFUL("Bountiful Pickaxe", "bountiful"),
    TITAN("Titan Pickaxe", "titan"),
    GOD("God Pickaxe", "god"),
    SWIFTCASTER("Swiftcaster Rod", "swiftcaster"),
    HELLFIRE("Hellfire Rod", "hellfire");

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