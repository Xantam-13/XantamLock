package com.xantamlock.core.tool;

import com.xantamlock.core.XantamLock;
import com.xantamlock.core.integration.VaultIntegration;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles tracking for punch mode and tool mode for players using XantamLock.
 */
public class LockToolHandler {

    // Tracks players with punch mode active (single-use)
    private static final Set<UUID> punchEnabled = new HashSet<>();

    // Tracks players with tool mode active (persistent)
    private static final Set<UUID> toolEnabled = new HashSet<>();

    /**
     * Enables punch mode for a player and charges economy if enabled.
     *
     * @param player The player's UUID.
     */
    public static void enablePunch(UUID player) {
        VaultIntegration vault = XantamLock.getInstance().getVaultIntegration();
        double cost = XantamLock.getInstance().getConfig().getDouble("economy.add-entity-cost", 0.0);

        if (vault != null && vault.isEnabled() && cost > 0) {
            if (!vault.charge(player, cost)) return;
        }
        punchEnabled.add(player);
    }

    /**
     * Checks if a player currently has punch mode enabled.
     *
     * @param player The player's UUID.
     * @return true if punch is enabled, false otherwise.
     */
    public static boolean hasPunch(UUID player) {
        return punchEnabled.contains(player);
    }

    /**
     * Consumes (disables) punch mode for a player.
     *
     * @param player The player's UUID.
     */
    public static void consumePunch(UUID player) {
        punchEnabled.remove(player);
    }

    /**
     * Enables the persistent tool mode for a player and charges economy if enabled.
     *
     * @param player The player's UUID.
     */
    public static void enableTool(UUID player) {
        VaultIntegration vault = XantamLock.getInstance().getVaultIntegration();
        double cost = XantamLock.getInstance().getConfig().getDouble("economy.lock-create-cost", 0.0);

        if (vault != null && vault.isEnabled() && cost > 0) {
            if (!vault.charge(player, cost)) return;
        }
        toolEnabled.add(player);
    }

    /**
     * Disables the persistent tool mode for a player.
     *
     * @param player The player's UUID.
     */
    public static void disableTool(UUID player) {
        toolEnabled.remove(player);
    }

    /**
     * Checks if a player has the tool mode enabled.
     *
     * @param player The player's UUID.
     * @return true if the tool is active, false otherwise.
     */
    public static boolean isToolEnabled(UUID player) {
        return toolEnabled.contains(player);
    }
}
