package com.xantamlock.core.lock;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a lock owned by a player.
 * A lock can secure multiple parts (blocks/entities) and can be protected with a password.
 */
public class Lock {

    private final String id;
    private String name;
    private final UUID owner;
    private LockMode mode;
    private final Set<String> parts = new HashSet<>();
    private final Set<String> trustedPlayers = new HashSet<>();
    private final Set<String> trustedFactions = new HashSet<>();
    private String password = null;

    public Lock(String id, String name, UUID owner, LockMode mode) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public UUID getOwner() {
        return owner;
    }

    public LockMode getMode() {
        return mode;
    }

    public void setMode(LockMode mode) {
        this.mode = mode;
    }

    public Set<String> getParts() {
        return parts;
    }

    public void addPart(String loc) {
        parts.add(loc);
    }

    public void removePart(String loc) {
        parts.remove(loc);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pw) {
        this.password = pw;
    }

    public Set<String> getTrustedPlayers() {
        return trustedPlayers;
    }

    public Set<String> getTrustedFactions() {
        return trustedFactions;
    }

    public void addTrustedPlayer(String uuid) {
        trustedPlayers.add(uuid);
    }

    public void removeTrustedPlayer(String uuid) {
        trustedPlayers.remove(uuid);
    }

    public void addTrustedFaction(String factionId) {
        trustedFactions.add(factionId);
    }

    public void removeTrustedFaction(String factionId) {
        trustedFactions.remove(factionId);
    }
}
