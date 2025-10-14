package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import java.util.Optional;

/**
 * NoteRow represents a single note entry within the sidebar list.
 * It shows the note title and a menu button with rename and delete actions.
 */
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

        // === Left Side: Note Icon + Title ===
        HBox leftContent = new HBox();
        leftContent.setAlignment(Pos.CENTER_LEFT);
        leftContent.setSpacing(10);
        leftContent.getStyleClass().add("note-row-left");

        SVGPath noteIconShape = new SVGPath();
        noteIconShape.setContent("M9 12H7v2h2v-2zm0-4H7v2h2V8zm4 0h-2v2h2V8zm0 4h-2v2h2v-2zm0-8H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z");
        noteIconShape.getStyleClass().add("note-icon-graphic");

        StackPane iconContainer = new StackPane(noteIconShape);
        iconContainer.getStyleClass().add("note-icon");

        Label titleLabel = new Label(note.getTitle());
        titleLabel.setWrapText(false);
        titleLabel.getStyleClass().add("note-title");

        leftContent.getChildren().addAll(iconContainer, titleLabel);
        setLeft(leftContent);

        // === Right Side: Action Buttons ===
        HBox rightContent = new HBox();
        rightContent.setAlignment(Pos.CENTER_RIGHT);
        rightContent.setSpacing(4);
        rightContent.getStyleClass().add("note-row-actions");

        Button menuButton = createIconButton(
                "M6 10a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0z",
                "note-menu-button"
        );

        rightContent.getChildren().add(menuButton);
        setRight(rightContent);

        // === Context Menu ===
        ContextMenu contextMenu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> renameNote());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.getStyleClass().add("destructive-menu-item");
        deleteItem.setOnAction(e -> deleteNote());

        contextMenu.getItems().addAll(renameItem, new SeparatorMenuItem(), deleteItem);
        menuButton.setOnAction(e -> contextMenu.show(menuButton, Side.BOTTOM, 0, 5));

        // === Click Handler ===
        this.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Button || event.getTarget() instanceof SVGPath)) {
                noteSelectionListener.onNoteSelected(note);
            }
        });
    }

    /**
     * Creates an icon-styled button for the row actions.
     */
    private Button createIconButton(String svgContent, String extraStyleClass) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().add("svg-path");

        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().add("icon-button");
        if (extraStyleClass != null && !extraStyleClass.isEmpty()) {
            button.getStyleClass().add(extraStyleClass);
        }
        button.setFocusTraversable(false);
        return button;
    }

    /**
     * Opens a dialog to rename the note.
     */
    private void renameNote() {
        TextInputDialog dialog = new TextInputDialog(note.getTitle());
        dialog.setTitle("Rename Note");
        dialog.setHeaderText("Enter new name for the note \"" + note.getTitle() + "\":");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.trim().equals(note.getTitle())) {
                note.setTitle(newName.trim());
                onNoteChangedCallback.run();
                noteSelectionListener.onNoteRenamed(note);
            }
        });
    }

    /**
     * Deletes the note after user confirmation.
     */
    private void deleteNote() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Note");
        alert.setHeaderText("Delete the note \"" + note.getTitle() + "\"?");
        alert.setContentText("This action cannot be undone.");

        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            appState.deleteNote(note.getId());
            EurekaApp.getSearchService().deleteNote(note);
            noteSelectionListener.onNoteDeleted(note);
            onNoteChangedCallback.run();
        }
    }

    /**
     * Applies or removes the active styling for the row.
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
