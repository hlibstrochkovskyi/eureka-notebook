package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.Note;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

// Temporary stub until we rewrite EditorContainer in JavaFX
public class EditorContainer extends StackPane implements NoteSelectionListener {
    public EditorContainer() {
        this.setStyle("-fx-background-color: white;");
        this.getChildren().add(new Label("Editor Area"));
    }

    @Override
    public void onNoteSelected(Note note) {
        // add some sht after
    }

    @Override
    public void onNoteDeleted(Note deletedNote) {
        // not now
    }

    @Override
    public void onSetDeleted(String setId) {
        // not now
    }
}
