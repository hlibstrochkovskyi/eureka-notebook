package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.SVGPath;

import java.util.Optional;

public class NoteRow extends BorderPane {

    private final Note note;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private final Runnable onNoteChangedCallback;

    public NoteRow(Note note, NoteSelectionListener listener, Runnable onNoteChangedCallback) {
        this.note = note;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onNoteChangedCallback = onNoteChangedCallback;

        getStyleClass().add("note-row");

        Label titleLabel = new Label(note.getTitle());

        // --- Icon Button for Actions ---
        Button menuButton = createIconButton("M6 10a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0z");

        // --- Context Menu ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> renameNote());
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteNote());
        contextMenu.getItems().addAll(renameItem, deleteItem);

        menuButton.setOnAction(e -> contextMenu.show(menuButton, Side.BOTTOM, 0, 5));

        this.setLeft(titleLabel);
        this.setRight(menuButton);

        this.setOnMouseClicked(event -> {
            // Trigger selection only if not clicking on the button area
            if (!(event.getTarget() instanceof Button || event.getTarget() instanceof SVGPath)) {
                noteSelectionListener.onNoteSelected(note);
            }
        });
    }

    /**
     * Helper method to create a styled button with an SVG icon.
     * @param svgContent The SVG path data for the icon.
     * @return A configured Button.
     */
    private Button createIconButton(String svgContent) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().add("svg-path");

        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().add("icon-button");
        return button;
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
                onNoteChangedCallback.run(); // This refreshes the note list in the sidebar
                noteSelectionListener.onNoteRenamed(note); // This updates the tab if open
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