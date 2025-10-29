package com.eureka;

import com.eureka.model.Note;
import java.util.List;

/**
 * Interface definition for a callback mechanism to handle events related to note selection and modification.
 * Components interested in reacting to these events (like the EditorContainer or Sidebar)
 * should implement this interface.
 */
public interface NoteSelectionListener {

    /**
     * Called when a note is selected by the user (e.g., clicked in the sidebar).
     * The implementing component should typically display the content of this note.
     *
     * @param note The {@link Note} object that was selected. Can be null if selection is cleared.
     */
    void onNoteSelected(Note note);

    /**
     * Called after a note has been successfully deleted.
     * Implementing components might need to close tabs or remove UI elements related to this note.
     *
     * @param deletedNote The {@link Note} object that was deleted.
     */
    void onNoteDeleted(Note deletedNote);

    /**
     * Called after an entire note set (and all notes within it) has been deleted.
     * Implementing components might need to close multiple tabs or update UI elements.
     *
     * @param setId        The ID of the {@link com.eureka.model.NoteSet} that was deleted.
     * @param deletedNotes A list of {@link Note} objects that were part of the deleted set.
     */
    void onSetDeleted(String setId, List<Note> deletedNotes);

    /**
     * Called after a note has been renamed.
     * Implementing components might need to update tab titles or other UI elements displaying the note's name.
     *
     * @param renamedNote The {@link Note} object that was renamed, containing the new title.
     */
    void onNoteRenamed(Note renamedNote);

    /**
     * Called when a note is selected specifically from the search results.
     * Allows the implementing component to not only select the note but also potentially
     * highlight the searched text within the editor.
     *
     * @param note      The {@link Note} object selected from the search results.
     * @param position  The starting character index of the found search query within the note's content.
     * @param wordIndex The approximate word index where the search term was found (useful for context).
     * @param query     The actual search query string that led to this selection.
     */
    void onNoteSelectedFromSearch(Note note, int position, int wordIndex, String query);
}