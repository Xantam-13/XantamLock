package com.xantamlock.core.integration;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class FactionsIntegration {

    private boolean enabled;

    public FactionsIntegration() {
        this.enabled = Bukkit.getPluginManager().getPlugin("Factions3") != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Example stub method to demonstrate faction ownership.
     * Extend this later with actual Factions3 support.
     */
    public boolean isSameFaction(Player p1, OfflinePlayer p2) {
        if (!enabled) return false;
        // Add real Factions3 API integration here later
        return false;
    }
}
