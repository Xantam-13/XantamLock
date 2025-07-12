package com.xantamlock.core.tool;

import com.xantamlock.core.lock.Lock;
import com.xantamlock.core.lock.LockFocusTracker;
import com.xantamlock.core.lock.LockManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class VisualiserHandler {

    private static final Map<UUID, Integer> activeVisuals = new HashMap<>();

    public static void toggleVisual(Player player, boolean enable) {
        UUID uuid = player.getUniqueId();

        if (enable) {
            if (activeVisuals.containsKey(uuid)) return;

            Lock lock = LockManager.getFocusedLock(uuid);
            if (lock == null || lock.getParts().isEmpty()) {
                player.sendMessage(color("&6[XantamLock]&c No focused lock or no parts to visualise."));
                return;
            }

            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    Bukkit.getPluginManager().getPlugin("XantamLock"), () -> {
                        for (String locString : lock.getParts()) {
                            Location loc = deserialize(locString);
                            if (loc != null) {
                                loc.getWorld().spawnParticle(Particle.HEART, loc.clone().add(0.5, 1.2, 0.5), 4, 0.3, 0.3, 0.3, 0);
                            }
                        }
                    },
                    0L, 20L // every second
            );

            activeVisuals.put(uuid, taskId);
        } else {
            if (activeVisuals.containsKey(uuid)) {
                Bukkit.getScheduler().cancelTask(activeVisuals.remove(uuid));
            }
        }
    }

    public static void stopAll() {
        for (int taskId : activeVisuals.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        activeVisuals.clear();
    }

    private static Location deserialize(String locString) {
        try {
            String[] split = locString.split(",");
            if (split.length != 4) return null;
            World world = Bukkit.getWorld(split[0]);
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int z = Integer.parseInt(split[3]);
            return new Location(world, x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    private static String color(String msg) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', msg);
    }
}
