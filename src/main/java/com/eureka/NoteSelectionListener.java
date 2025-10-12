// FIX #1: The package must be 'com.eureka'
package com.eureka;

import com.eureka.model.Note;

/**
 * An interface to handle communication between the Sidebar and the EditorContainer.
 * This acts as a contract for what events can be passed between them.
 */
public interface NoteSelectionListener {

    /**
     * Called when a note is selected in the sidebar.
     * @param note The note that was selected.
     */
    void onNoteSelected(Note note);

    /**
     * FIX #2: Add the new method for handling note deletion to the contract.
     * Called when a note is deleted.
     * @param deletedNote The note that was deleted.
     */
    void onNoteDeleted(Note deletedNote);

    /**
     * FIX #3: Add the new method for handling set deletion to the contract.
     * Called when an entire set of notes is deleted.
     * @param setId The ID of the set that was deleted.
     */
    void onSetDeleted(String setId);
}

