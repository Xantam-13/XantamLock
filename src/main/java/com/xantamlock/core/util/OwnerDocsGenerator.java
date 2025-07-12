package com.xantamlock.core.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OwnerDocsGenerator {

    private final Plugin plugin;

    public OwnerDocsGenerator(Plugin plugin) {
        this.plugin = plugin;
    }

    public void generate() {
        File ownerDir = new File(plugin.getDataFolder(), "owners");
        if (!ownerDir.exists()) ownerDir.mkdirs();

        writeFile(ownerDir, "commands.txt", getCommandsDoc());
        writeFile(ownerDir, "permissions.txt", getPermissionsDoc());
        writeFile(ownerDir, "placeholders.txt", getPlaceholdersDoc());
        writeFile(ownerDir, "modules.txt", getModulesDoc());
    }

    private void writeFile(File dir, String filename, String content) {
        try {
            FileWriter fw = new FileWriter(new File(dir, filename));
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            Bukkit.getLogger().warning("[XantamLock] Failed to write " + filename + ": " + e.getMessage());
        }
    }

    private String getCommandsDoc() {
        return String.join("\n", new String[] {
            "/lock create <name>",
            "/lock edit name <old> <new>",
            "/lock edit password set/remove/edit <value>",
            "/lock delete <name> yes",
            "/lock transfer <yourname> <target> <name> yes",
            "/lock punch",
            "/lock tool",
            "/lock list",
            "/lock show [name]",
            "/lock use <name|that|this>",
            "/lock visualise on/off [name]"
        });
    }

    private String getPermissionsDoc() {
        return String.join("\n", new String[] {
            "xantamlock.command.lock",
            "xantamlock.admin",
            "xantamlock.use",
            "xantamlock.create",
            "xantamlock.edit",
            "xantamlock.delete",
            "xantamlock.transfer"
        });
    }

    private String getPlaceholdersDoc() {
        return String.join("\n", new String[] {
            "%xantamlock_focused_lock%",
            "%xantamlock_total_locks%"
        });
    }

    private String getModulesDoc() {
        return String.join("\n", new String[] {
            "Module 1: Core locking system.",
            "Module 2: Lock access roles.",
            "Module 3: Shop support.",
            "Module 4: Redstone, hoppers, and autoclose."
        });
    }
}
