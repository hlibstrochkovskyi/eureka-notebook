package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SetRow - Компонент для отображения набора заметок (папки)
 * Содержит заголовок с иконкой, кнопками действий и развернутый список заметок
 */
public class SetRow extends VBox {

    private final NoteSet noteSet;
    private final VBox notesPanel;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private boolean isExpanded = false;
    private final Runnable onSetChangedCallback;
    private final Label setNameLabel;
    private final SVGPath arrowIcon;
    private final SVGPath setIcon;

    public SetRow(NoteSet noteSet, NoteSelectionListener listener, Runnable onSetChangedCallback) {
        this.noteSet = noteSet;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onSetChangedCallback = onSetChangedCallback;

        getStyleClass().add("set-row");

        // === Header Panel ===
        HBox headerPanel = new HBox(8);
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.getStyleClass().add("header");
        headerPanel.setPadding(new Insets(4, 0, 4, 0));

        // Expand/Collapse Arrow
        arrowIcon = new SVGPath();
        arrowIcon.setContent("M8 5l6 6-6 6z");
        arrowIcon.getStyleClass().add("arrow-icon");

        Region arrowContainer = new Region();
        arrowContainer.setShape(arrowIcon);
        arrowContainer.getStyleClass().add("arrow-icon");
        arrowContainer.setMinWidth(20);
        arrowContainer.setMinHeight(20);

        // Set Icon (folder-like icon)
        setIcon = createSetIcon();
        Region setIconContainer = new Region();
        setIconContainer.setShape(setIcon);
        setIconContainer.getStyleClass().add("set-icon-container");
        setIconContainer.setMinWidth(18);
        setIconContainer.setMinHeight(18);

        // Set Name Label
        this.setNameLabel = new Label(noteSet.getName());
        setNameLabel.setStyle("-fx-font-weight: semibold; -fx-font-size: 13;");

        // Spacer for alignment
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // === Action Buttons ===
        Button addButton = createActionButton(
                "M12 5v14m-7-7h14",  // Plus icon
                "Add new note",
                e -> addNewNote()
        );

        Button menuButton = createActionButton(
                "M12 8a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4z",  // Vertical dots
                "More options",
                null
        );

        // === Context Menu ===
        ContextMenu contextMenu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Rename Set");
        renameItem.setOnAction(e -> renameSet());

        MenuItem deleteItem = new MenuItem("Delete Set");
        deleteItem.getStyleClass().add("destructive-menu-item");
        deleteItem.setOnAction(e -> deleteSet());

        contextMenu.getItems().addAll(renameItem, new SeparatorMenuItem(), deleteItem);
        menuButton.setOnAction(e -> contextMenu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 5));

        // Add everything to header
        headerPanel.getChildren().addAll(
                arrowContainer, setIconContainer, setNameLabel, spacer, addButton, menuButton
        );

        // === Notes Panel (collapsible) ===
        notesPanel = new VBox(0);
        notesPanel.getStyleClass().add("notes-panel");
        notesPanel.setVisible(false);
        notesPanel.setManaged(false);

        this.getChildren().addAll(headerPanel, notesPanel);

        // === Event Handlers ===
        headerPanel.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Button || event.getTarget() instanceof SVGPath)) {
                toggleExpand();
            }
        });
    }

    /**
     * Создает кнопку действия с иконкой SVG
     */
    private Button createActionButton(String svgContent, String tooltip, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().add("svg-path");

        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().add("icon-button");
        button.setTooltip(new Tooltip(tooltip));

        if (handler != null) {
            button.setOnAction(handler);
        }

        return button;
    }

    /**
     * Создает иконку папки для сета
     */
    private SVGPath createSetIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 7a2 2 0 012-2h4l2 2h4a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V7z");
        icon.getStyleClass().add("svg-path");
        return icon;
    }

    /**
     * Переключает расширение/сворачивание панели заметок
     */
    private void toggleExpand() {
        isExpanded = !isExpanded;

        // Анимация стрелки
        RotateTransition rt = new RotateTransition(Duration.millis(200), arrowIcon);
        rt.setToAngle(isExpanded ? 90 : 0);
        rt.play();

        notesPanel.setVisible(isExpanded);
        notesPanel.setManaged(isExpanded);

        if (isExpanded) {
            refreshNotesList();
        }
    }

    /**
     * Добавляет новую заметку в этот сет
     */
    private void addNewNote() {
        if (!isExpanded) {
            toggleExpand();
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Note");
        dialog.setHeaderText("Enter title for note in \"" + noteSet.getName() + "\":");
        dialog.setContentText("Title:");

        // Стилизация диалога
        dialog.getDialogPane().getStyleClass().add("styled-dialog");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(title -> {
            if (!title.trim().isEmpty()) {
                Note newNote = new Note(noteSet.getId(), title.trim());
                appState.addNote(newNote);
                EurekaApp.getSearchService().addOrUpdateNote(newNote);
                refreshNotesList();
                noteSelectionListener.onNoteSelected(newNote);
            }
        });
    }

    /**
     * Удаляет весь сет и все его заметки
     */
    private void deleteSet() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Set");
        alert.setHeaderText("Delete the set \"" + noteSet.getName() + "\"?");
        alert.setContentText("This will permanently delete the set and all notes within it.\nThis action cannot be undone.");

        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            List<Note> notesToDelete = List.copyOf(appState.getNotesForSet(noteSet.getId()));
            notesToDelete.forEach(note -> EurekaApp.getSearchService().deleteNote(note));
            appState.deleteSet(noteSet.getId());
            noteSelectionListener.onSetDeleted(noteSet.getId(), notesToDelete);
            onSetChangedCallback.run();
        }
    }

    /**
     * Переименовывает сет
     */
    private void renameSet() {
        TextInputDialog dialog = new TextInputDialog(noteSet.getName());
        dialog.setTitle("Rename Set");
        dialog.setHeaderText("Enter new name for the set:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.trim().equals(noteSet.getName())) {
                noteSet.setName(newName.trim());
                this.setNameLabel.setText(noteSet.getName());
                onSetChangedCallback.run();
            }
        });
    }

    /**
     * Обновляет список заметок в панели
     */
    private void refreshNotesList() {
        notesPanel.getChildren().clear();
        List<Note> notesInSet = appState.getNotesForSet(noteSet.getId());

        if (notesInSet.isEmpty()) {
            Label emptyLabel = new Label("No notes in this set");
            emptyLabel.getStyleClass().add("empty-set-label");
            notesPanel.getChildren().add(emptyLabel);
        } else {
            for (Note note : notesInSet) {
                NoteRow noteRow = new NoteRow(note, noteSelectionListener, this::refreshNotesList);
                notesPanel.getChildren().add(noteRow);
            }
        }
    }

    // === Public API Methods ===

    public NoteSet getNoteSet() {
        return noteSet;
    }

    public void expand() {
        if (!isExpanded) {
            toggleExpand();
        }
    }

    public List<NoteRow> getNoteRows() {
        return notesPanel.getChildren().stream()
                .filter(node -> node instanceof NoteRow)
                .map(node -> (NoteRow) node)
                .collect(Collectors.toList());
    }
}