package com.xantamlock.core.lock;

import com.xantamlock.core.database.LockStorage;

import java.util.*;

public class LockManager {

    private static final Map<UUID, List<Lock>> playerLocks = new HashMap<>();
    private static final Map<UUID, String> focusedLocks = new HashMap<>();

    public static Lock createLock(UUID owner, String name) {
        String id = UUID.randomUUID().toString();
        Lock lock = new Lock(id, name, owner, LockMode.PRIVATE);
        playerLocks.computeIfAbsent(owner, k -> new ArrayList<>()).add(lock);
        LockStorage.saveLock(lock);
        return lock;
    }

    public static List<Lock> getLocks(UUID player) {
        return playerLocks.getOrDefault(player, Collections.emptyList());
    }

    public static Lock getLockByName(UUID player, String name) {
        return getLocks(player).stream()
                .filter(lock -> lock.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static Lock getLockById(UUID player, String id) {
        return getLocks(player).stream()
                .filter(lock -> lock.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public static void setFocusedLock(UUID player, String lockId) {
        focusedLocks.put(player, lockId);
    }

    public static String getFocusedLockId(UUID player) {
        return focusedLocks.get(player);
    }

    public static Lock getFocusedLock(UUID player) {
        String lockId = focusedLocks.get(player);
        return lockId != null ? getLockById(player, lockId) : null;
    }

    public static boolean hasLockWithName(UUID player, String name) {
        return getLockByName(player, name) != null;
    }

    public static void loadLock(UUID player, Lock lock) {
        playerLocks.computeIfAbsent(player, k -> new ArrayList<>()).add(lock);
    }

    public static void removeLock(UUID player, Lock lock) {
        getLocks(player).remove(lock);
        if (Objects.equals(focusedLocks.get(player), lock.getId())) {
            focusedLocks.remove(player);
        }
    }

    /**
     * Returns the lock that owns this part (e.g., block location string), or null.
     */
    public static Lock getLockByPart(String loc) {
        for (List<Lock> lockList : playerLocks.values()) {
            for (Lock lock : lockList) {
                if (lock.getParts().contains(loc)) {
                    return lock;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a player is allowed to access a given lock.
     * For now, only PRIVATE locks restrict access.
     */
    public static boolean canAccess(UUID player, Lock lock) {
        if (lock == null) return false;
        if (lock.getMode() == LockMode.PUBLIC) return true;
        return lock.getOwner().equals(player);
    }
}
