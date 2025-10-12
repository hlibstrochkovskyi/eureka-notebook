package com.eureka;

import com.eureka.model.Note;
import java.util.List;

public interface NoteSelectionListener {
    void onNoteSelected(Note note);
    void onNoteDeleted(Note deletedNote);
    // Pass the list of notes that were deleted along with the set
    void onSetDeleted(String setId, List<Note> deletedNotes);

    // Add this method to the interface
    void onNoteRenamed(Note renamedNote);

}

