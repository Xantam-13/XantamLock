package com.xantamlock.core.tool;

import com.xantamlock.core.lock.Lock;
import com.xantamlock.core.lock.LockManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VisualiserHandler {

    private static final Set<UUID> visualising = new HashSet<>();
    private static final Map<UUID, BukkitRunnable> tasks = new HashMap<>();

    public static void toggleVisual(Player player, boolean on) {
        UUID uuid = player.getUniqueId();
        if (on) {
            if (visualising.contains(uuid)) return;
            visualising.add(uuid);
            startTask(player);
        } else {
            visualising.remove(uuid);
            stopTask(uuid);
        }
    }

    public static boolean isVisualising(UUID uuid) {
        return visualising.contains(uuid);
    }

    private static void startTask(Player player) {
        UUID uuid = player.getUniqueId();

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!visualising.contains(uuid)) {
                    cancel();
                    return;
                }

                Lock lock = LockManager.getFocusedLock(uuid);
                if (lock == null) return;

                for (String locStr : lock.getParts()) {
                    String[] parts = locStr.split(",");
                    if (parts.length < 4) continue;

                    Location loc = new Location(
                            Bukkit.getWorld(parts[0]),
                            Integer.parseInt(parts[1]) + 0.5,
                            Integer.parseInt(parts[2]) + 1.2,
                            Integer.parseInt(parts[3]) + 0.5
                    );

                    if (loc.getWorld() != null) {
                        player.spawnParticle(Particle.HEART, loc, 1, 0, 0, 0, 0);
                    }
                }
            }
        };

        task.runTaskTimer(com.xantamlock.core.XantamLock.getInstance(), 0L, 40L);
        tasks.put(uuid, task);
    }

    private static void stopTask(UUID uuid) {
        BukkitRunnable task = tasks.remove(uuid);
        if (task != null) task.cancel();
    }
}
