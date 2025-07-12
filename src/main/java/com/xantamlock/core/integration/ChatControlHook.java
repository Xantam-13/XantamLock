package com.xantamlock.core.integration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class ChatControlHook {

    private final boolean enabled;

    public ChatControlHook() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ChatControl");
        this.enabled = plugin != null && plugin.isEnabled();
        if (enabled) {
            Bukkit.getLogger().info("[XantamLock] ChatControl integration enabled.");
        } else {
            Bukkit.getLogger().info("[XantamLock] ChatControl not found, skipping integration.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    // Optional future API handling here
}
