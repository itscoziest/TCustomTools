package com.titancustomtools.listeners;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.enums.ToolType;
import com.titancustomtools.listeners.abilities.HellfireAbility;
import com.titancustomtools.listeners.abilities.SwiftcasterAbility;
import com.titancustomtools.managers.ToolManager;
import com.titancustomtools.utils.DropHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Random;

public class FishingListener implements Listener {

    private final TitanCustomTools plugin;
    private final ToolManager toolManager;
    private final SwiftcasterAbility swiftcasterAbility;
    private final HellfireAbility hellfireAbility;
    private final Random random = new Random();

    private final Material[] fishTypes = {Material.COD, Material.SALMON, Material.PUFFERFISH, Material.TROPICAL_FISH};

    public FishingListener(TitanCustomTools plugin) {
        this.plugin = plugin;
        this.toolManager = plugin.getToolManager();
        this.swiftcasterAbility = new SwiftcasterAbility(plugin);
        this.hellfireAbility = new HellfireAbility(plugin);
    }

    /**
     * CRITICAL FIX:
     * Priority = HIGHEST (Runs after Anti-Fishing plugins)
     * ignoreCancelled = true (If Anti-Fishing cancels it, we stop)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) return;

        ToolType toolType = toolManager.getToolType(item);
        if (toolType == null) return;

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Location hookLoc = event.getHook().getLocation();
            handleRodRewards(player, toolType, hookLoc);
            toolManager.decrementUse(player, item);
        }

        if (toolType == ToolType.SWIFTCASTER && event.getState() == PlayerFishEvent.State.FISHING) {
            swiftcasterAbility.handleFishing(event);
        }

        if (toolType == ToolType.HELLFIRE) {
            if (event.getState() == PlayerFishEvent.State.FISHING) {
                hellfireAbility.handleCast(event);
            } else {
                hellfireAbility.handleReel(event, item);
            }
        }
    }

    private void handleRodRewards(Player player, ToolType type, Location hookLoc) {
        double roll = random.nextDouble();

        switch (type) {
            case TITAN_ROD:
                // Default lowered to 0.002 (0.2% or 1 in 500)
                // If you want it harder, lower 'point-chance' in config.yml
                double titanChance = plugin.getConfig().getDouble("titan-rod.point-chance", 0.002);
                double doubleChance = plugin.getConfig().getDouble("titan-rod.double-chance", 0.0001);

                if (roll < titanChance) {
                    int pts = 1;
                    if (random.nextDouble() < doubleChance) {
                        pts = 2;
                    }

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "titanpoints give " + player.getName() + " " + pts);

                    // NEW MESSAGE FORMAT
                    String plural = (pts > 1) ? "s" : "";
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&6&lTITAN POINTS! &7You found &e" + pts + " Titan Point" + plural + " &7while fishing!"));
                }
                break;

            case TICKET_ROD:
                double ticketChance = plugin.getConfig().getDouble("ticket-rod.chance", 0.25);
                if (roll < ticketChance) {
                    int amount = 20 + random.nextInt(81);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tickets give " + player.getName() + " " + amount);
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "You fished up " + amount + " Tickets!");
                }
                break;

            case TOKEN_ROD:
                double tokenChance = plugin.getConfig().getDouble("token-rod.chance", 0.25);
                if (roll < tokenChance) {
                    int amount = 20 + random.nextInt(81);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tokens give " + player.getName() + " " + amount);
                    player.sendMessage(ChatColor.YELLOW + "You fished up " + amount + " Tokens!");
                }
                break;

            case CRATE_ROD:
                double crateChance = plugin.getConfig().getDouble("crate-rod.key-chance", 0.05);
                if (roll < crateChance) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate key give " + player.getName() + " fish 1");
                    player.sendMessage(ChatColor.GOLD + "You fished up a Crate Key!");
                }
                break;

            case LIGHTNING_ROD:
                double strikeChance = plugin.getConfig().getDouble("lightning-rod.strike-chance", 0.15);
                if (roll < strikeChance) {
                    hookLoc.getWorld().strikeLightningEffect(hookLoc);
                    player.sendMessage(ChatColor.WHITE + "A lucky strike! You caught extra fish!");

                    int fishCount = 2 + random.nextInt(4);
                    for (int i = 0; i < fishCount; i++) {
                        Material randomFish = fishTypes[random.nextInt(fishTypes.length)];
                        DropHelper.handleDrop(player, new ItemStack(randomFish), player.getLocation());
                    }
                }
                break;
        }
    }
}