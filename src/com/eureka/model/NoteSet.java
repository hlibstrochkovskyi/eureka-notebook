package com.eureka.model;

import java.util.UUID;

/**
 * Represents a single set (or folder) of notes.
 */
public class NoteSet {
    private final String id;
    private String name;
    private final long createdAt;

    public NoteSet(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}