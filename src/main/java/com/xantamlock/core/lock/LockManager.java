package com.xantamlock.core.lock;

import com.xantamlock.core.XantamLock;
import com.xantamlock.core.database.LockStorage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.\*;

public class LockManager {

```
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

    public static Lock getLockByPart(String part) {
        for (List<Lock> locks : playerLocks.values()) {
            for (Lock lock : locks) {
                if (lock.getParts().contains(part)) {
                    return lock;
                }
            }
        }
        return null;
    }

    public static boolean canAccess(Player player, Lock lock) {
        UUID playerId = player.getUniqueId();
        if (lock.getOwner().equals(playerId)) return true;
        if (lock.getTrustedPlayers().contains(playerId.toString())) return true;

        if (XantamLock.getInstance().getFactionsIntegration().isEnabled()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(playerId);
            for (String factionId : lock.getTrustedFactions()) {
                if (XantamLock.getInstance().getFactionsIntegration().isSameFaction(player, offline)) {
                    return true;
                }
            }
        }

        return false;
    }
```

}
