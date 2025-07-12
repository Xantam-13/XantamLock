package com.xantamlock.core.database;

import com.xantamlock.core.XantamLock;
import com.xantamlock.core.lock.Lock;
import com.xantamlock.core.lock.LockManager;
import com.xantamlock.core.lock.LockMode;

import java.sql.*;
import java.util.*;

public class LockStorage {

    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS locks (
            id VARCHAR(64) PRIMARY KEY,
            owner VARCHAR(36),
            name VARCHAR(255),
            mode VARCHAR(16),
            parts TEXT,
            password TEXT,
            trusted_players TEXT,
            trusted_factions TEXT
        );
        """;

    public static void init() {
        try (Connection conn = XantamLock.getInstance().getDatabaseManager().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            XantamLock.getInstance().getLogger().severe("[XantamLock] Failed to create locks table.");
            e.printStackTrace();
        }
    }

    public static void saveLock(Lock lock) {
        String sql = "MERGE INTO locks (id, owner, name, mode, parts, password, trusted_players, trusted_factions) KEY(id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = XantamLock.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lock.getId());
            ps.setString(2, lock.getOwner().toString());
            ps.setString(3, lock.getName());
            ps.setString(4, lock.getMode().name());

            ps.setString(5, String.join(";", lock.getParts()));
            ps.setString(6, lock.getPassword() == null ? "" : lock.getPassword());
            ps.setString(7, String.join(";", lock.getTrustedPlayers()));
            ps.setString(8, String.join(";", lock.getTrustedFactions()));

            ps.executeUpdate();
        } catch (SQLException e) {
            XantamLock.getInstance().getLogger().severe("[XantamLock] Failed to save lock: " + lock.getName());
            e.printStackTrace();
        }
    }

    public static void loadAll() {
        String sql = "SELECT * FROM locks";

        try (Connection conn = XantamLock.getInstance().getDatabaseManager().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    String id = rs.getString("id");
                    UUID owner = UUID.fromString(rs.getString("owner"));
                    String name = rs.getString("name");
                    LockMode mode = LockMode.valueOf(rs.getString("mode"));
                    String partsRaw = rs.getString("parts");
                    String password = rs.getString("password");
                    String trustedPlayersRaw = rs.getString("trusted_players");
                    String trustedFactionsRaw = rs.getString("trusted_factions");

                    Lock lock = new Lock(id, name, owner, mode);

                    if (partsRaw != null && !partsRaw.isBlank()) {
                        Arrays.stream(partsRaw.split(";")).forEach(lock::addPart);
                    }

                    if (password != null && !password.isBlank()) {
                        lock.setPassword(password);
                    }

                    if (trustedPlayersRaw != null && !trustedPlayersRaw.isBlank()) {
                        Arrays.stream(trustedPlayersRaw.split(";")).forEach(lock::addTrustedPlayer);
                    }

                    if (trustedFactionsRaw != null && !trustedFactionsRaw.isBlank()) {
                        Arrays.stream(trustedFactionsRaw.split(";")).forEach(lock::addTrustedFaction);
                    }

                    LockManager.loadLock(owner, lock);

                } catch (Exception ex) {
                    XantamLock.getInstance().getLogger().warning("[XantamLock] Failed to load a lock from DB. Skipping entry.");
                    ex.printStackTrace();
                }
            }

        } catch (SQLException e) {
            XantamLock.getInstance().getLogger().severe("[XantamLock] Failed to load locks from database.");
            e.printStackTrace();
        }
    }
}
