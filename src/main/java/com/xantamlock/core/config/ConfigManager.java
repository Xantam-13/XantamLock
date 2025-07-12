package com.xantamlock.core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigManager {

    private static FileConfiguration config;

    public static void load(Plugin plugin) {
        config = plugin.getConfig();
    }

    public static boolean useMySQL() {
        return config.getBoolean("database.use-mysql", false);
    }

    public static double getLockCreateCost() {
        return config.getDouble("economy.lock-create-cost", 0.0);
    }

    public static double getAddEntityCost() {
        return config.getDouble("economy.add-entity-cost", 0.0);
    }

    public static double getConvertToShopCost() {
        return config.getDouble("economy.convert-to-shop-cost", 0.0);
    }

    public static String getChatPrefix() {
        return config.getString("chat.prefix", "&6[XantamLock]&f");
    }

    public static String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public static int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public static String getMySQLDatabase() {
        return config.getString("database.mysql.database", "xantamlock");
    }

    public static String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public static String getMySQLPassword() {
        return config.getString("database.mysql.password", "");
    }
}
