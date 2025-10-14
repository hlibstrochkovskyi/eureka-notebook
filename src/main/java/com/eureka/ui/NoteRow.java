package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;

import java.util.Optional;

/**
 * NoteRow - Компонент для отображения одной заметки в боковой панели
 * Показывает название заметки и кнопки действий (переименование, удаление)
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
        this.setPadding(new Insets(6, 8, 6, 12));

        // === Left Side: Note Icon + Title ===
        HBox leftContent = new HBox(8);
        leftContent.setAlignment(Pos.CENTER_LEFT);

        // Note icon
        SVGPath noteIcon = new SVGPath();
        noteIcon.setContent("M9 12H7v2h2v-2zm0-4H7v2h2V8zm4 0h-2v2h2V8zm0 4h-2v2h2v-2zm0-8H3c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z");
        noteIcon.getStyleClass().add("svg-path");
        noteIcon.setStyle("-fx-stroke-width: 1.2; -fx-scale-x: 0.75; -fx-scale-y: 0.75;");

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

        Button menuButton = createIconButton(
                "M6 10a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0zm6 0a2 2 0 11-4 0 2 2 0 014 0z"  // Dots
        );

        rightContent.getChildren().add(menuButton);
        this.setRight(rightContent);

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
     * Создает иконку-кнопку
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
     * Открывает диалог переименования заметки
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
     * Удаляет заметку с подтверждением
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
     * Устанавливает активное/неактивное состояние (для подсветки)
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