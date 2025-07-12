package com.xantamlock.core.integration;

import com.xantamlock.core.lock.LockFocusTracker;
import com.xantamlock.core.lock.LockManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "xantamlock";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Xantam";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Keep expansion loaded across reloads
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (!player.isOnline()) return "";

        switch (identifier.toLowerCase()) {
            case "focused_lock":
                String id = LockFocusTracker.getFocusedLockId(player.getUniqueId());
                if (id == null) return "None";
                return LockManager.getLockById(player.getUniqueId(), id).getName();
            case "total_locks":
                return String.valueOf(LockManager.getLocks(player.getUniqueId()).size());
            default:
                return null;
        }
    }
}
