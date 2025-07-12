package com.xantamlock.core.database;

import com.xantamlock.core.XantamLock;
import com.xantamlock.core.config.ConfigManager;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private Connection connection;
    private XantamLock plugin;

    public void init(XantamLock plugin) {
        this.plugin = plugin;
        connect();
    }

    private void connect() {
        try {
            if (ConfigManager.useMySQL()) {
                String host = plugin.getConfig().getString("database.mysql.host");
                int port = plugin.getConfig().getInt("database.mysql.port");
                String db = plugin.getConfig().getString("database.mysql.database");
                String user = plugin.getConfig().getString("database.mysql.username");
                String pass = plugin.getConfig().getString("database.mysql.password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&autoReconnect=true";
                connection = DriverManager.getConnection(url, user, pass);
                plugin.getLogger().info("[XantamLock] Connected to MySQL database.");
            } else {
                Class.forName("org.h2.Driver");
                File dbFile = new File(plugin.getDataFolder(), "xantamlockdb");
                String url = "jdbc:h2:file:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url, "sa", "");
                plugin.getLogger().info("[XantamLock] Connected to local H2 database.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[XantamLock] Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                plugin.getLogger().warning("[XantamLock] Database connection was closed. Reconnecting...");
                connect();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[XantamLock] Error checking DB connection: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("[XantamLock] Failed to close DB connection.");
                e.printStackTrace();
            }
        }
    }
}
