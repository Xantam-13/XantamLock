package com.xantamlock.core.util;

import com.xantamlock.core.XantamLock;
import com.xantamlock.core.config.ConfigManager;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OwnerDocsGenerator {

    private final XantamLock plugin;

    public OwnerDocsGenerator(XantamLock plugin) {
        this.plugin = plugin;
    }

    public void generate() {
        File folder = new File(plugin.getDataFolder(), "owner_docs");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, "reference.txt");
        try (FileWriter writer = new FileWriter(file)) {

            writer.write("====== XantamLock Owner Reference ======\n\n");

            // Plugin version
            writer.write("Plugin Version: " + plugin.getDescription().getVersion() + "\n\n");

            // Economy settings
            writer.write("Economy:\n");
            writer.write("  lock-create-cost: " + ConfigManager.getLockCreateCost() + "\n");
            writer.write("  add-entity-cost: " + ConfigManager.getAddEntityCost() + "\n");
            writer.write("  convert-to-shop-cost: " + ConfigManager.getConvertToShopCost() + "\n\n");

            // Chat prefix
            writer.write("Chat:\n");
            writer.write("  prefix: " + ConfigManager.getChatPrefix() + "\n\n");

            // Database mode
            writer.write("Database:\n");
            writer.write("  Mode: " + (ConfigManager.useMySQL() ? "MySQL" : "H2") + "\n\n");

            // Integrations
            writer.write("Soft Integrations:\n");
            writer.write("  Vault: " + (plugin.getVaultIntegration().isEnabled() ? "✅ Enabled" : "❌ Disabled") + "\n");
            writer.write("  Factions3: " + (plugin.getFactionsIntegration().isEnabled() ? "✅ Enabled" : "❌ Disabled") + "\n");
            writer.write("  PlaceholderAPI: " + (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ? "✅ Detected" : "❌ Not Detected") + "\n");
            writer.write("  ChatControl: " + (plugin.getChatControlHook().isEnabled() ? "✅ Enabled" : "❌ Disabled") + "\n\n");

            // Permissions
            writer.write("Permissions:\n");
            writer.write("  xantamlock.admin - Grants full access to all commands (default: OP)\n\n");

            // Commands
            writer.write("Commands:\n");
            writer.write("  /lock create <name>        - Create a new lock\n");
            writer.write("  /lock use <name|this|that> - Switch focus to a lock\n");
            writer.write("  /lock list                 - View your locks\n");
            writer.write("  /lock show [name]          - View info about a lock\n");
            writer.write("  /lock edit name <old> <new> - Rename a lock\n");
            writer.write("  /lock punch                - Enable single punch mode\n");
            writer.write("  /lock tool                 - Receive locking tool\n");
            writer.write("  /lock visualise on|off     - Toggle lock particle display\n\n");

            writer.write("Placeholders (via PlaceholderAPI):\n");
            writer.write("  %xantamlock_active_name%     - Name of currently focused lock\n");
            writer.write("  %xantamlock_active_mode%     - Mode of focused lock\n");
            writer.write("  %xantamlock_active_parts%    - Part count of focused lock\n");

            writer.write("\n========================================\n");

        } catch (IOException e) {
            plugin.getLogger().severe("[XantamLock] Failed to generate owner_docs/reference.txt");
            e.printStackTrace();
        }
    }
}
