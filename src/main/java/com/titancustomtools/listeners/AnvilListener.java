package com.titancustomtools.listeners;

import com.titancustomtools.TitanCustomTools;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class AnvilListener implements Listener {

    private final TitanCustomTools plugin;

    public AnvilListener(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        // Get the two items in the input slots
        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);

        // Basic null checks
        if (first == null || second == null || first.getType() == Material.AIR || second.getType() == Material.AIR) {
            return;
        }

        // Check which items are Custom Tools
        boolean firstIsCustom = plugin.getToolManager().isCustomTool(first);
        boolean secondIsCustom = plugin.getToolManager().isCustomTool(second);

        // --- RULE 1: Block Custom + Custom ---
        if (firstIsCustom && secondIsCustom) {
            event.setResult(null);
            return;
        }

        // --- RULE 2: Handle Custom (Left) + Normal/Book (Right) ---
        if (firstIsCustom) {
            // Check compatibility (Same type or Enchanted Book)
            if (second.getType() != Material.ENCHANTED_BOOK && second.getType() != first.getType()) {
                return;
            }

            // 1. Create the Result Item (Clone of the Custom Tool to keep abilities)
            ItemStack resultItem = first.clone();

            // 2. Determine Enchantments to add
            Map<Enchantment, Integer> sacrificeEnchants;
            if (second.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) second.getItemMeta();
                sacrificeEnchants = meta.getStoredEnchants();
            } else {
                sacrificeEnchants = second.getEnchantments();
            }

            Map<Enchantment, Integer> currentEnchants = resultItem.getEnchantments();

            int xpCost = 0;
            boolean anyChange = false;

            // 3. Merge Enchantments
            for (Map.Entry<Enchantment, Integer> entry : sacrificeEnchants.entrySet()) {
                Enchantment enchant = entry.getKey();
                int sacrificeLevel = entry.getValue();
                int currentLevel = currentEnchants.getOrDefault(enchant, 0);

                // Check if valid for this item type (unless it already has it)
                if (!enchant.canEnchantItem(resultItem) && !resultItem.containsEnchantment(enchant)) {
                    continue;
                }

                // Check conflicts
                boolean conflict = false;
                for (Enchantment existing : currentEnchants.keySet()) {
                    if (!existing.equals(enchant) && existing.conflictsWith(enchant)) {
                        conflict = true;
                        break;
                    }
                }
                if (conflict) continue;

                // Calculate Levels
                int finalLevel = currentLevel;
                if (sacrificeLevel > currentLevel) {
                    finalLevel = sacrificeLevel;
                } else if (sacrificeLevel == currentLevel) {
                    if (currentLevel < enchant.getMaxLevel()) {
                        finalLevel = currentLevel + 1;
                    }
                }

                // If something changed, add to cost and apply
                if (finalLevel != currentLevel) {
                    resultItem.addUnsafeEnchantment(enchant, finalLevel);
                    xpCost += (finalLevel * 2); // Simple cost formula: 2 levels per enchant level
                    anyChange = true;
                }
            }

            // 4. Handle Rename
            String renameText = event.getInventory().getRenameText();
            // Check if the name actually changed from the original item
            if (renameText != null && !renameText.isEmpty()) {
                ItemMeta meta = resultItem.getItemMeta();
                if (!renameText.equals(meta.getDisplayName())) {
                    meta.setDisplayName(renameText);
                    resultItem.setItemMeta(meta);
                    xpCost += 1;
                    anyChange = true;
                }
            }

            // 5. Apply Result and Set Repair Cost
            if (anyChange) {
                // Ensure a minimum cost so the server accepts the transaction
                if (xpCost <= 0) xpCost = 5;

                // Limit cost to prevent "Too Expensive" (Vanilla max is 40)
                if (xpCost > 39) xpCost = 39;

                // IMPORTANT: You MUST set the Repair Cost on the inventory
                event.getInventory().setRepairCost(xpCost);
                event.setResult(resultItem);
            }
        }
    }
}