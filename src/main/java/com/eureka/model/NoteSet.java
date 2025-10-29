package com.eureka.model;

import java.util.UUID;

/**
 * Represents a single set (or folder) of notes.
 * Each set has a unique ID, a name, and a creation timestamp.
 */
public class NoteSet {
    // Unique identifier for the note set (final, cannot be changed)
    private final String id;
    // Name of the note set (can be changed)
    private String name;
    // Timestamp when the note set was created (final, cannot be changed)
    private final long createdAt;

    /**
     * Constructs a new NoteSet with the given name.
     * Generates a unique UUID for the ID and sets the creation timestamp
     * to the current time.
     * @param name The initial name for the note set.
     */
    public NoteSet(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Gets the unique identifier of this note set.
     * @return The UUID string representing the note set's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the current name of this note set.
     * @return The name of the note set.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name for this note set.
     * @param name The new name to assign to the note set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the timestamp (in milliseconds since the epoch) when this note set was created.
     * @return The creation timestamp.
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the name of the note set.
     * This is used, for example, when displaying the NoteSet object in UI lists.
     * @return The name of the note set.
     */
    @Override
    public String toString() {
        return name;
    }
}