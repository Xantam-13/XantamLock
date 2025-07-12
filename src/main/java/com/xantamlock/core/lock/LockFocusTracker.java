package com.xantamlock.core.lock;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which lock a player is currently focusing on.
 */
public class LockFocusTracker {

    private static final Map<UUID, String> focusedLocks = new ConcurrentHashMap<>();

    public static void setFocusedLock(UUID player, String lockId) {
        focusedLocks.put(player, lockId);
    }

    public static String getFocusedLockId(UUID player) {
        return focusedLocks.get(player);
    }

    public static void clearFocusedLock(UUID player) {
        focusedLocks.remove(player);
    }

    public static boolean hasFocusedLock(UUID player) {
        return focusedLocks.containsKey(player);
    }
}
