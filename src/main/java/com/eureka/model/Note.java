package com.eureka.model;

import java.util.UUID;

/**
 * Represents a single note within a NoteSet.
 * Each note has a unique ID, belongs to a specific set (setId), has a title,
 * content, and creation/update timestamps.
 */
public class Note {
    // Unique identifier for the note (final, cannot be changed)
    private final String id;
    // Identifier of the NoteSet this note belongs to (final, cannot be changed)
    private final String setId;
    // Title of the note (can be changed)
    private String title;
    // Main content/body of the note (can be changed)
    private String content;
    // Timestamp when the note was created (final, cannot be changed)
    private final long createdAt;
    // Timestamp when the note was last updated (can be changed)
    private long updatedAt;

    /**
     * Constructs a new Note with the given setId and title.
     * Generates a unique UUID for the note's ID, initializes content as empty,
     * and sets both creation and update timestamps to the current time.
     * @param setId The ID of the NoteSet this note belongs to.
     * @param title The initial title for the note.
     */
    public Note(String setId, String title) {
        this.id = UUID.randomUUID().toString(); // Generate a random unique ID
        this.setId = setId;
        this.title = title;
        this.content = ""; // Initialize content as empty
        long now = System.currentTimeMillis(); // Get current time
        this.createdAt = now; // Set creation time
        this.updatedAt = now; // Set initial update time same as creation
    }

    // --- Getters ---

    /**
     * Gets the unique identifier of this note.
     * @return The UUID string representing the note's ID.
     */
    public String getId() { return id; }

    /**
     * Gets the ID of the NoteSet this note belongs to.
     * @return The UUID string of the parent NoteSet.
     */
    public String getSetId() { return setId; }

    /**
     * Gets the current title of this note.
     * @return The title string.
     */
    public String getTitle() { return title; }

    /**
     * Gets the main content (body) of this note.
     * @return The content string.
     */
    public String getContent() { return content; }

    /**
     * Gets the timestamp (in milliseconds since the epoch) when this note was created.
     * @return The creation timestamp.
     */
    public long getCreatedAt() { return createdAt; }

    /**
     * Gets the timestamp (in milliseconds since the epoch) when this note was last updated.
     * @return The last update timestamp.
     */
    public long getUpdatedAt() { return updatedAt; }

    // --- Setters ---

    /**
     * Sets a new title for this note.
     * Remember to also call setUpdatedAt() after changing the title.
     * @param title The new title string.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Sets the main content (body) for this note.
     * Remember to also call setUpdatedAt() after changing the content.
     * @param content The new content string.
     */
    public void setContent(String content) { this.content = content; }

    /**
     * Updates the 'last updated' timestamp for this note.
     * Should be called whenever the title or content is modified.
     * @param updatedAt The timestamp (usually System.currentTimeMillis()) to set as the last update time.
     */
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}