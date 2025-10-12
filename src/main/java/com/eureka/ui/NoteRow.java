package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import java.util.Optional;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class NoteRow extends BorderPane {
    private final Note note;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private final Runnable onNoteChangedCallback; // Renamed for clarity

    public NoteRow(Note note, NoteSelectionListener listener, Runnable onNoteChangedCallback) {
        this.note = note;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onNoteChangedCallback = onNoteChangedCallback;

        // --- UI Elements ---
        Label titleLabel = new Label(note.getTitle());
        titleLabel.setStyle("-fx-font-size: 13px;");

        // --- Actions Menu Button ---
        Button actionsButton = new Button("⋮"); // "More actions" button

        // --- Context Menu for Actions ---
        ContextMenu contextMenu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> renameNote());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteNote());

        contextMenu.getItems().addAll(renameItem, deleteItem);

        // Show context menu when actionsButton is clicked
        actionsButton.setOnAction(e -> contextMenu.show(actionsButton, Side.BOTTOM, 0, 0));

        // --- Layout ---
        this.setPadding(new Insets(6, 8, 6, 8));
        this.setLeft(titleLabel);
        this.setRight(actionsButton); // Use actionsButton instead of a simple delete button
        this.getStyleClass().add("note-row");
        this.setOnMouseClicked(event -> noteSelectionListener.onNoteSelected(note));
    }

    private void renameNote() {
        TextInputDialog dialog = new TextInputDialog(note.getTitle());
        dialog.setTitle("Rename Note");
        dialog.setHeaderText("Enter new name for the note \"" + note.getTitle() + "\":");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.trim().equals(note.getTitle())) {
                note.setTitle(newName.trim());
                // We need to tell the UI to refresh
                onNoteChangedCallback.run();
                // We also need to update the tab if it's open
                noteSelectionListener.onNoteRenamed(note);
            }
        });
    }

    private void deleteNote() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Note");
        alert.setHeaderText("Delete the note \"" + note.getTitle() + "\"?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            appState.deleteNote(note.getId());
            EurekaApp.getSearchService().deleteNote(note);
            noteSelectionListener.onNoteDeleted(note);
            onNoteChangedCallback.run();
        }
    }

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