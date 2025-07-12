package com.xantamlock.core;

import com.xantamlock.core.command.LockCommand;
import com.xantamlock.core.command.LockTabCompleter;
import com.xantamlock.core.config.ConfigManager;
import com.xantamlock.core.database.DatabaseManager;
import com.xantamlock.core.database.LockStorage;
import com.xantamlock.core.integration.*;
import com.xantamlock.core.listener.LockListener;
import com.xantamlock.core.util.OwnerDocsGenerator;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class XantamLock extends JavaPlugin {

    private static XantamLock instance;
    private DatabaseManager databaseManager;

    private VaultIntegration vaultIntegration;
    private FactionsIntegration factionsIntegration;
    private PlaceholderHook placeholderHook;
    private ChatControlHook chatControlHook;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        ConfigManager.load(this);

        databaseManager = new DatabaseManager();
        databaseManager.init(this);

        // Initialize integrations
        vaultIntegration = new VaultIntegration(this);
        factionsIntegration = new FactionsIntegration();
        placeholderHook = new PlaceholderHook();
        chatControlHook = new ChatControlHook();

        LockStorage.init();

        if (databaseManager.getConnection() != null) {
            LockStorage.loadAll();
        } else {
            getLogger().severe("[XantamLock] Skipping lock loading — no active DB connection.");
        }

        PluginCommand command = getCommand("lock");
        if (command != null) {
            command.setExecutor(new LockCommand());
            command.setTabCompleter(new LockTabCompleter());
        }

        getServer().getPluginManager().registerEvents(new LockListener(), this);

        new OwnerDocsGenerator(this).generate();

        getLogger().info("XantamLock Core has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("XantamLock Core has been disabled.");
    }

    public static XantamLock getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }

    public FactionsIntegration getFactionsIntegration() {
        return factionsIntegration;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public ChatControlHook getChatControlHook() {
        return chatControlHook;
    }
}
