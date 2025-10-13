package com.eureka.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppState {
    private static AppState instance;
    // These must not be final for Gson to be able to load data into them
    private List<NoteSet> sets;
    private List<Note> notes;


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

    private boolean sidebarCollapsed = false;

    public boolean isSidebarCollapsed() {
        return sidebarCollapsed;
    }

    public void setSidebarCollapsed(boolean sidebarCollapsed) {
        this.sidebarCollapsed = sidebarCollapsed;
    }


    // Method to create a new, empty state, used when no save file exists
    public static AppState createEmptyState() {
        instance = new AppState();
        return instance;
    }

    // Method to replace the current state with a loaded one
    public static void loadInstance(AppState loadedState) {
        instance = loadedState;
        // Ensure lists are not null if the save file was corrupted or empty
        if (instance.sets == null) {
            instance.sets = new ArrayList<>();
        }
        if (instance.notes == null) {
            instance.notes = new ArrayList<>();
        }
    }

    public List<NoteSet> getSets() {
        return sets;
    }

    public void addSet(NoteSet set) {
        sets.add(set);
    }

    public void deleteSet(String setId) {
        sets.removeIf(set -> set.getId().equals(setId));
        notes.removeIf(note -> note.getSetId().equals(setId));
    }


    // Add these two new methods to your AppState.java class

    public Optional<NoteSet> getSetById(String setId) {
        return sets.stream().filter(set -> set.getId().equals(setId)).findFirst();
    }

    public List<Note> getAllNotes() {
        return new ArrayList<>(notes);
    }


    public List<Note> getNotesForSet(String setId) {
        return notes.stream()
                .filter(note -> note.getSetId().equals(setId))
                .collect(Collectors.toList());
    }

    public void addNote(Note note) {
        notes.add(note);
    }

    public void deleteNote(String noteId) {
        notes.removeIf(note -> note.getId().equals(noteId));
    }

    public Optional<Note> getNoteById(String noteId) {
        return notes.stream().filter(note -> note.getId().equals(noteId)).findFirst();
    }

}