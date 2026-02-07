package com.titancustomtools.listeners.abilities;

import com.titancustomtools.TitanCustomTools;
import com.titancustomtools.utils.DropHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class HellfireAbility {

    private final TitanCustomTools plugin;
    private final Random random = new Random();

    // Store active fishing tasks
    private final Map<UUID, LavaFishingTask> activeTasks = new HashMap<>();

    public HellfireAbility(TitanCustomTools plugin) {
        this.plugin = plugin;
    }

    public void handleCast(PlayerFishEvent event) {
        if (activeTasks.containsKey(event.getPlayer().getUniqueId())) {
            activeTasks.get(event.getPlayer().getUniqueId()).cancel();
            activeTasks.remove(event.getPlayer().getUniqueId());
        }

        FishHook hook = event.getHook();
        Player player = event.getPlayer();

        LavaFishingTask task = new LavaFishingTask(player, hook);
        task.runTaskTimer(plugin, 1, 5); // Runs every 5 ticks (0.25s)
        activeTasks.put(player.getUniqueId(), task);
    }

    public void handleReel(PlayerFishEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!activeTasks.containsKey(uuid)) {
            return;
        }

        LavaFishingTask task = activeTasks.get(uuid);

        if (task.state == FishingState.SEARCHING) {
            task.cancel();
            activeTasks.remove(uuid);
            return;
        }

        if (task.state == FishingState.WAITING) {
            // Player pulled too early -> Allow retrieval, send message
            player.sendMessage(ChatColor.RED + "Too Early! You pulled the hook out before a bite.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);

            task.cancel();
            activeTasks.remove(uuid);
            return;
        }

        if (task.state == FishingState.BITING) {
            event.setCancelled(true);
            task.cancel();
            activeTasks.remove(uuid);

            if (event.getHook() != null) event.getHook().remove();

            giveLavaLoot(player);

            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "You caught something from the lava!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            player.playSound(player.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, 1f, 1.0f);
        }
    }

    private void giveLavaLoot(Player player) {
        int roll = random.nextInt(100);
        ItemStack loot;

        if (roll < 50) {
            loot = new ItemStack(Material.QUARTZ, random.nextInt(5) + 3);
        } else if (roll < 80) {
            loot = new ItemStack(Material.NETHER_BRICK, random.nextInt(5) + 3);
        } else {
            loot = new ItemStack(Material.MAGMA_CREAM, random.nextInt(3) + 1);
        }

        DropHelper.handleDrop(player, loot, player.getLocation());
    }

    private enum FishingState {
        SEARCHING, WAITING, BITING
    }

    private class LavaFishingTask extends BukkitRunnable {
        private final Player player;
        private final FishHook hook;

        FishingState state = FishingState.SEARCHING;

        private int waitTimer = -1;
        private int biteWindowTimer = 0;

        public LavaFishingTask(Player player, FishHook hook) {
            this.player = player;
            this.hook = hook;
        }

        @Override
        public void run() {
            if (hook == null || hook.isDead() || !player.isOnline()) {
                this.cancel();
                activeTasks.remove(player.getUniqueId());
                return;
            }

            // --- SEARCHING ---
            if (state == FishingState.SEARCHING) {
                Block block = hook.getLocation().getBlock();
                if (block.getType() == Material.LAVA) {
                    state = FishingState.WAITING;
                    hook.setInvulnerable(true);

                    // Wait 30s-60s (approx 120-240 units)
                    waitTimer = 120 + random.nextInt(121);

                    player.playSound(player.getLocation(), Sound.BLOCK_LAVA_AMBIENT, 1f, 1f);
                }
                return;
            }

            // Physics (Runs for WAITING & BITING)
            hook.setGravity(false);
            hook.setVelocity(new Vector(0, 0.04, 0));

            // --- WAITING ---
            if (state == FishingState.WAITING) {
                waitTimer--;

                // FIX: Send title EVERY update loop (every 5 ticks).
                // FadeIn: 0, Stay: 20 (1s), FadeOut: 10
                // Since we update every 5 ticks, the 'Stay' of 20 ticks ensures it never disappears.
                player.sendTitle(
                        ChatColor.GOLD + "Catching...",
                        ChatColor.YELLOW + "Wait for it...",
                        0, 20, 10
                );

                if (waitTimer <= 0) {
                    state = FishingState.BITING;
                    biteWindowTimer = 6 + random.nextInt(10);

                    player.sendTitle(
                            ChatColor.GREEN + "!!! BITE !!!",
                            ChatColor.AQUA + "RIGHT CLICK NOW!",
                            0, 20, 10
                    );
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 2.0f);
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1f, 1f);
                    hook.getWorld().spawnParticle(Particle.LAVA, hook.getLocation(), 20, 0.5, 0.5, 0.5, 0.2);
                }
                return;
            }

            // --- BITING ---
            if (state == FishingState.BITING) {
                biteWindowTimer--;

                if (biteWindowTimer % 2 == 0) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 2f);
                }

                if (biteWindowTimer <= 0) {
                    player.sendTitle(ChatColor.RED + "Got away!", "", 0, 20, 10);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);

                    this.cancel();
                    activeTasks.remove(player.getUniqueId());
                    hook.remove();
                }
            }
        }
    }
}