package com.eureka.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Singleton class representing the overall state of the application.
 * Holds lists of note sets and notes, and provides methods to manage them.
 */
public class AppState {
    private static AppState instance;

    private List<NoteSet> sets;
    private List<Note> notes;

    private boolean sidebarCollapsed = false;

    /**
     * Private constructor to prevent direct instantiation (Singleton pattern).
     * Initializes empty lists for sets and notes.
     */
    private AppState() {
        sets = new ArrayList<>();
        notes = new ArrayList<>();
    }

    /**
     * Gets the single instance of the AppState (Singleton pattern).
     * Creates the instance if it doesn't exist yet.
     * Ensures thread safety using 'synchronized'.
     * @return The singleton AppState instance.
     */
    public static synchronized AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    /**
     * Checks if the sidebar is currently collapsed.
     * @return true if the sidebar is collapsed, false otherwise.
     */
    public boolean isSidebarCollapsed() {
        return sidebarCollapsed;
    }

    /**
     * Sets the collapsed state of the sidebar.
     * @param sidebarCollapsed true to collapse the sidebar, false to expand it.
     */
    public void setSidebarCollapsed(boolean sidebarCollapsed) {
        this.sidebarCollapsed = sidebarCollapsed;
    }


    /**
     * Creates a new, empty AppState instance.
     * Used when no save file exists or loading fails.
     * @return A new AppState instance with empty lists.
     */
    public static AppState createEmptyState() {
        instance = new AppState();
        return instance;
    }

    /**
     * Replaces the current singleton instance with a loaded state.
     * Ensures that the lists within the loaded state are not null,
     * initializing them as empty ArrayLists if they are.
     * @param loadedState The AppState instance loaded from storage (e.g., JSON file).
     */
    public static void loadInstance(AppState loadedState) {
        instance = loadedState;
        if (instance.sets == null) {
            instance.sets = new ArrayList<>();
        }
        if (instance.notes == null) {
            instance.notes = new ArrayList<>();
        }
    }

    /**
     * Gets the list of all note sets.
     * @return A list containing all NoteSet objects.
     */
    public List<NoteSet> getSets() {
        return sets;
    }

    /**
     * Adds a new note set to the application state.
     * @param set The NoteSet object to add.
     */
    public void addSet(NoteSet set) {
        sets.add(set);
    }

    /**
     * Deletes a note set and all notes associated with it.
     * Removes the set from the 'sets' list and corresponding notes from the 'notes' list.
     * @param setId The unique ID of the NoteSet to delete.
     */
    public void deleteSet(String setId) {
        sets.removeIf(set -> set.getId().equals(setId));
        notes.removeIf(note -> note.getSetId().equals(setId));
    }


    /**
     * Finds a NoteSet by its unique ID.
     * @param setId The ID of the NoteSet to find.
     * @return An Optional containing the NoteSet if found, or an empty Optional otherwise.
     */
    public Optional<NoteSet> getSetById(String setId) {
        return sets.stream()
                .filter(set -> set.getId().equals(setId))
                .findFirst();
    }

    /**
     * Gets a copy of the list containing all notes in the application.
     * Returns a new ArrayList to prevent modification of the internal list.
     * @return A new list containing all Note objects.
     */
    public List<Note> getAllNotes() {
        // Return a copy to prevent external modification of the internal list
        return new ArrayList<>(notes);
    }


    /**
     * Gets all notes belonging to a specific note set.
     * @param setId The unique ID of the parent NoteSet.
     * @return A list of Note objects that belong to the specified set.
     */
    public List<Note> getNotesForSet(String setId) {
        return notes.stream()
                .filter(note -> note.getSetId().equals(setId))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new note to the application state.
     * @param note The Note object to add.
     */
    public void addNote(Note note) {
        notes.add(note);
    }

    /**
     * Deletes a note based on its unique ID.
     * @param noteId The ID of the Note to delete.
     */
    public void deleteNote(String noteId) {
        notes.removeIf(note -> note.getId().equals(noteId));
    }

    /**
     * Finds a Note by its unique ID.
     * @param noteId The ID of the Note to find.
     * @return An Optional containing the Note if found, or an empty Optional otherwise.
     */
    public Optional<Note> getNoteById(String noteId) {
        return notes.stream()
                .filter(note -> note.getId().equals(noteId))
                .findFirst();
    }
}