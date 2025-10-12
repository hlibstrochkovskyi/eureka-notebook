package com.eureka.model;

import java.util.UUID;

/**
 * Represents a single note.
 */
public class Note {
    private final String id;
    private final String setId;
    private String title;
    private String content;
    private final long createdAt;
    private long updatedAt;

    public Note(String setId, String title) {
        this.id = UUID.randomUUID().toString();
        this.setId = setId;
        this.title = title;
        this.content = "";
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Getters
    public String getId() { return id; }
    public String getSetId() { return setId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
