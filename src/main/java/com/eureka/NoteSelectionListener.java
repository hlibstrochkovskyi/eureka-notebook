package com.eureka;

import com.eureka.model.Note;
import java.util.List;

public interface NoteSelectionListener {
    void onNoteSelected(Note note);

    void onNoteDeleted(Note deletedNote);

    void onSetDeleted(String setId, List<Note> deletedNotes);

    void onNoteRenamed(Note renamedNote);

    void onNoteSelectedFromSearch(Note note, int position, int wordIndex, String query);
}
