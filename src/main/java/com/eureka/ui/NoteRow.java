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
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import java.util.Optional;

/**
 * NoteRow renders an individual note entry inside the set panel.
 * It shows the note title, a note icon, and a menu for actions.
 */
public class NoteRow extends BorderPane {

    private final Note note;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private final Runnable onNoteChangedCallback;
    private final Label titleLabel;

    public NoteRow(Note note, NoteSelectionListener listener, Runnable onNoteChangedCallback) {
        this.note = note;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onNoteChangedCallback = onNoteChangedCallback;

        getStyleClass().add("note-row");

        HBox leftContent = new HBox();
        leftContent.setAlignment(Pos.CENTER_LEFT);
        leftContent.getStyleClass().add("note-content");

        SVGPath noteIcon = new SVGPath();
        noteIcon.setContent("M9 12H7v2h2v-2zm0-4H7v2h2V8zm4 0h-2v2h2V8zm0 4h-2v2h2v-2zm0-8H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z");
        noteIcon.getStyleClass().addAll("svg-path", "note-icon");

        StackPane iconWrapper = new StackPane(noteIcon);
        iconWrapper.getStyleClass().add("note-icon-wrapper");

        titleLabel = new Label(note.getTitle());
        titleLabel.getStyleClass().add("note-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        leftContent.getChildren().addAll(iconWrapper, titleLabel);
        setLeft(leftContent);

        HBox rightContent = new HBox();
        rightContent.setAlignment(Pos.CENTER_RIGHT);
        rightContent.getStyleClass().add("note-actions");

        Button menuButton = createIconButton(
                "M6 10a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0z"
        );
        menuButton.getStyleClass().add("note-menu-button");

        rightContent.getChildren().add(menuButton);
        setRight(rightContent);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("note-context-menu");

        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(event -> renameNote());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.getStyleClass().add("destructive-menu-item");
        deleteItem.setOnAction(event -> deleteNote());

        contextMenu.getItems().addAll(renameItem, new SeparatorMenuItem(), deleteItem);
        menuButton.setOnMouseClicked(mouseEvent -> {
            mouseEvent.consume();
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            } else {
                contextMenu.show(menuButton, Side.BOTTOM, 0, 6);
            }
        });

        setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Button || event.getTarget() instanceof SVGPath)) {
                noteSelectionListener.onNoteSelected(note);
            }
        });
    }

    /**
     * Builds the icon-only button used for the note menu.
     */
    private Button createIconButton(String svgContent) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().addAll("svg-path", "icon-button-graphic");

        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().addAll("icon-button", "note-action-button");
        button.setFocusTraversable(false);
        return button;
    }

    /**
     * Opens the rename dialog and updates the note title.
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
                titleLabel.setText(note.getTitle());
                onNoteChangedCallback.run();
                noteSelectionListener.onNoteRenamed(note);
            }
        });
    }

    /**
     * Deletes the note after confirmation.
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
     * Applies the visual active state when the note is selected.
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
