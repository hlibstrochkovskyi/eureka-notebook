// FIX #1: The package must be 'com.eureka.model'
package com.eureka.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Singleton class to hold the application's state (all notes and sets).
 * This ensures that all parts of the app are working with the same data.
 */
public class AppState {
    private static AppState instance;
    private final List<NoteSet> sets;
    private final List<Note> notes;

    private AppState() {
        sets = new ArrayList<>();
        notes = new ArrayList<>();
    }

    public static synchronized AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    public List<NoteSet> getSets() {
        return sets;
    }

    public void addSet(NoteSet set) {
        sets.add(set);
    }

    // FIX #3: Add the missing 'deleteSet' method.
    public void deleteSet(String setId) {
        sets.removeIf(set -> set.getId().equals(setId));
        // Also remove all notes associated with this set
        notes.removeIf(note -> note.getSetId().equals(setId));
    }

    public List<Note> getNotesForSet(String setId) {
        return notes.stream()
                .filter(note -> note.getSetId().equals(setId))
                .collect(Collectors.toList());
    }


    public void addNote(Note note) {
        notes.add(note);
    }

    // FIX #4: Add the missing 'deleteNote' method.
    public void deleteNote(String noteId) {
        notes.removeIf(note -> note.getId().equals(noteId));
    }
}