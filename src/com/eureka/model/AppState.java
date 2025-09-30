package com.eureka.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A singleton class to hold the application's state, such as lists of sets and notes.
 */
public class AppState {
    private static AppState instance;
    private final List<NoteSet> sets;
    private final List<Note> notes;

    private AppState() {
        sets = new ArrayList<>();
        notes = new ArrayList<>();
    }

    /**
     * Gets the single instance of the com.eureka.model.AppState.
     * @return The com.eureka.model.AppState instance.
     */
    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    public List<NoteSet> getSets() {
        return new ArrayList<>(sets); // Return a copy to prevent outside modification
    }

    public void addSet(NoteSet set) {
        if (set != null) {
            sets.add(set);
        }
    }

    public void addNote(Note note) {
        if (note != null) {
            notes.add(note);
        }
    }

    public List<Note> getNotesForSet(String setId) {
        return notes.stream()
                .filter(note -> note.getSetId().equals(setId))
                .collect(Collectors.toList());
    }

    public Note getNoteById(String noteId) {
        return notes.stream()
                .filter(note -> note.getId().equals(noteId))
                .findFirst()
                .orElse(null);
    }
}