package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;

import java.util.Optional;

/**
 * Represents a single row displaying a note within the sidebar's SetRow.
 * Shows the note's title, an icon, and action buttons (e.g., for a context menu).
 * Handles click events for note selection and context menu actions like rename and delete.
 */
public class NoteRow extends BorderPane {

    // The Note data object this row represents.
    private final Note note;
    // Listener to notify when a note is selected, renamed, or deleted.
    private final NoteSelectionListener noteSelectionListener;
    // Reference to the application's global state.
    private final AppState appState;
    // Callback function to run when changes occur (e.g., rename, delete)
    // that require the parent (SetRow) to refresh its list.
    private final Runnable onNoteChangedCallback;

    /**
     * Constructs a NoteRow UI component.
     * @param note                  The Note object to display.
     * @param listener              The listener to notify about note interactions.
     * @param onNoteChangedCallback A callback function to execute when the note list needs refreshing
     * (e.g., after deleting or renaming this note).
     */
    public NoteRow(Note note, NoteSelectionListener listener, Runnable onNoteChangedCallback) {
        this.note = note;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onNoteChangedCallback = onNoteChangedCallback;

        getStyleClass().add("note-row");
        this.setPadding(new Insets(6, 8, 6, 12));

        // === Left Side: Note Icon + Title ===
        HBox leftContent = new HBox(8);
        leftContent.setAlignment(Pos.CENTER_LEFT);

        // Note icon (using SVGPath)
        SVGPath noteIcon = new SVGPath();
        noteIcon.setContent("M9 12H7v2h2v-2zm0-4H7v2h2V8zm4 0h-2v2h2V8zm0 4h-2v2h2v-2zm0-8H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"); // SVG path data for a note icon
        noteIcon.getStyleClass().add("svg-path");
        noteIcon.setStyle("-fx-stroke-width: 1.2; -fx-scale-x: 0.75; -fx-scale-y: 0.75;");

        // Container for the icon to manage size and background
        Region iconContainer = new Region();
        iconContainer.setShape(noteIcon);
        iconContainer.getStyleClass().add("note-icon-container");
        iconContainer.setMinWidth(16);
        iconContainer.setMinHeight(16);
        iconContainer.setStyle("-fx-background-color: rgba(88, 101, 242, 0.3); -fx-background-radius: 4;");

        Label titleLabel = new Label(note.getTitle());
        titleLabel.setWrapText(false);
        titleLabel.setStyle("-fx-font-size: 13;");

        leftContent.getChildren().addAll(iconContainer, titleLabel);
        this.setLeft(leftContent);

        // === Right Side: Action Buttons ===
        HBox rightContent = new HBox(4);
        rightContent.setAlignment(Pos.CENTER_RIGHT);
        rightContent.setStyle("-fx-padding: 0;");

        // Menu button (three dots)
        Button menuButton = createIconButton(
                "M6 10a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0z"
        );

        // Add the menu button to the right HBox
        rightContent.getChildren().add(menuButton);
        this.setRight(rightContent);

        // === Context Menu ===
        ContextMenu contextMenu = new ContextMenu();

        // Menu item for renaming the note
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> renameNote());

        // Menu item for deleting the note
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
     * Creates an icon button using an SVG path for the graphic.
     * @param svgContent The string containing the SVG path data.
     * @return A configured Button with the SVG icon.
     */
    private Button createIconButton(String svgContent) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().add("svg-path");

        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().add("icon-button");
        button.setStyle("-fx-padding: 4; -fx-min-width: 28; -fx-min-height: 28;");
        return button;
    }

    /**
     * Opens a dialog to rename the current note.
     * If a new valid name is entered, it updates the Note object,
     * triggers the callback to refresh the parent list, and notifies the listener.
     */
    private void renameNote() {
        TextInputDialog dialog = new TextInputDialog(note.getTitle());
        dialog.setTitle("Rename Note");
        dialog.setHeaderText("Enter new name for the note \"" + note.getTitle() + "\":");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            String trimmedName = newName.trim();
            if (!trimmedName.isEmpty() && !trimmedName.equals(note.getTitle())) {
                note.setTitle(trimmedName);
                onNoteChangedCallback.run();
                noteSelectionListener.onNoteRenamed(note);
            }
        });
    }

    /**
     * Shows a confirmation dialog and deletes the note if confirmed.
     * Updates the application state, search service, notifies the listener,
     * and triggers the callback to refresh the parent list.
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
     * Sets the visual state of the row to active (highlighted) or inactive.
     * Adds or removes a CSS style class to change the appearance.
     * @param isActive true to set the row as active (highlighted), false for inactive.
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

    /**
     * Gets the Note object associated with this row.
     * @return The Note object.
     */
    public Note getNote() {
        return note;
    }
}