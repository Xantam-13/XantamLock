package com.xantamlock.core.lock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a secure lock owned by a player.
 * A lock can have multiple linked blocks/entities ("parts").
 */
public class Lock {

    private final String id;
    private final UUID owner;

    private String name;
    private LockMode mode;
    private final Set<String> parts = new HashSet<>();
    private String password;

    /**
     * Constructs a new Lock.
     *
     * @param id    Unique internal ID of the lock.
     * @param name  Display name of the lock.
     * @param owner UUID of the owning player.
     * @param mode  The lock's visibility mode.
     */
    public Lock(String id, String name, UUID owner, LockMode mode) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.mode = mode;
    }

    public String getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public LockMode getMode() {
        return mode;
    }

    public void setMode(LockMode mode) {
        this.mode = mode;
    }

    /**
     * Returns an unmodifiable view of all serialized parts.
     */
    public Set<String> getParts() {
        return Collections.unmodifiableSet(parts);
    }

    /**
     * Adds a serialized block/entity location to this lock.
     *
     * @param loc The location string.
     */
    public void addPart(String loc) {
        parts.add(loc);
    }

    /**
     * Removes a block/entity from this lock by location.
     *
     * @param loc The location string.
     */
    public void removePart(String loc) {
        parts.remove(loc);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pw) {
        this.password = pw;
    }

    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    public boolean containsPart(String loc) {
        return parts.contains(loc);
    }
}
