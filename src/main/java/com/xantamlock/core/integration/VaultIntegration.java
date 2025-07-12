package com.xantamlock.core.integration;

import com.xantamlock.core.XantamLock;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

/**
 * Handles integration with Vault-based economy plugins.
 */
public class VaultIntegration {

    private final Economy economy;
    private final boolean enabled;

    public VaultIntegration(XantamLock plugin) {
        Economy foundEconomy = null;
        boolean hookSuccess = false;

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null && rsp.getProvider() != null) {
                foundEconomy = rsp.getProvider();
                hookSuccess = true;
                plugin.getLogger().info("[XantamLock] Vault found and economy hooked.");
            } else {
                plugin.getLogger().warning("[XantamLock] Vault found but no economy provider detected.");
            }
        } else {
            plugin.getLogger().info("[XantamLock] Vault not found. Economy integration disabled.");
        }

        this.economy = foundEconomy;
        this.enabled = hookSuccess;
    }

    public boolean isEnabled() {
        return enabled && economy != null;
    }

    public boolean charge(UUID uuid, double amount) {
        if (!isEnabled()) return false;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (!economy.has(player, amount)) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(UUID uuid, double amount) {
        if (!isEnabled()) return false;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean hasEnough(UUID uuid, double amount) {
        if (!isEnabled()) return false;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        return economy.has(player, amount);
    }
}
