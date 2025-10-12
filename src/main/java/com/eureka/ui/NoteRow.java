package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.Optional;

/**
 * A JavaFX component that represents a single note row inside a SetRow.
 */
public class NoteRow extends BorderPane {
    private final Note note;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private final Runnable onNoteDeletedCallback;

    public NoteRow(Note note, NoteSelectionListener listener, Runnable onNoteDeletedCallback) {
        this.note = note;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onNoteDeletedCallback = onNoteDeletedCallback;

        // --- UI Elements ---

        Label titleLabel = new Label(note.getTitle());
        titleLabel.setStyle("-fx-font-size: 13px;");

        Button deleteButton = new Button("×");

        // --- Layout ---

        this.setPadding(new Insets(6, 8, 6, 8));
        this.setLeft(titleLabel);
        this.setRight(deleteButton);
        this.getStyleClass().add("note-row"); // Add a CSS class for styling

        // --- Event Handlers ---

        // Select the note when the row is clicked
        this.setOnMouseClicked(event -> {
            noteSelectionListener.onNoteSelected(note);
        });

        // Delete the note when the 'x' button is clicked
        deleteButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Note");
            alert.setHeaderText("Delete the note \"" + note.getTitle() + "\"?");
            alert.setContentText("This action cannot be undone.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                appState.deleteNote(note.getId());
                noteSelectionListener.onNoteDeleted(note);
                onNoteDeletedCallback.run(); // Refresh the parent SetRow's list
            }
        });
    }

    /**
     * Applies or removes the active style based on the selection state.
     * We will call this from EditorContainer later.
     */
    public void setActive(boolean isActive) {
        if (isActive) {
            if (!getStyleClass().contains("note-row-active")) {
                getStyleClass().add("note-row-active");
            }
        } else {
            getStyleClass().remove("note-row-active");
        }
    }

    public Note getNote() {
        return note;
    }
}