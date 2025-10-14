package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SetRow is a collapsible component used to display a note set (folder) in the sidebar.
 * It shows a header with an icon and actions, followed by an expandable list of notes.
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

    public SetRow(NoteSet noteSet, NoteSelectionListener listener, Runnable onSetChangedCallback) {
        this.noteSet = noteSet;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onSetChangedCallback = onSetChangedCallback;

        getStyleClass().add("set-row");

        // === Header Panel ===
        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.setSpacing(12);
        headerPanel.getStyleClass().add("set-row-header");

        // Expand/Collapse Arrow
        arrowIcon = new SVGPath();
        arrowIcon.setContent("M8 5l6 6-6 6z");
        arrowIcon.getStyleClass().add("arrow-icon-shape");

        StackPane arrowContainer = new StackPane(arrowIcon);
        arrowContainer.getStyleClass().add("arrow-icon");

        // Set Icon
        SVGPath setIconShape = createSetIcon();
        setIconShape.getStyleClass().add("set-icon-graphic");

        StackPane setIconContainer = new StackPane(setIconShape);
        setIconContainer.getStyleClass().add("set-icon");

        // Set Name Label
        this.setNameLabel = new Label(noteSet.getName());
        setNameLabel.getStyleClass().add("set-row-title");

        // Spacer for alignment
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // === Action Buttons ===
        Button addButton = createActionButton(
                "M12 5v14m-7-7h14",
                "Add new note",
                e -> addNewNote(),
                "add-note-button"
        );

        Button menuButton = createActionButton(
                "M12 8a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4z",
                "More options",
                null,
                "menu-button"
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

        // Assemble the header
        headerPanel.getChildren().addAll(
                arrowContainer,
                setIconContainer,
                setNameLabel,
                spacer,
                addButton,
                menuButton
        );

        // === Notes Panel (collapsible) ===
        notesPanel = new VBox();
        notesPanel.setSpacing(4);
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
     * Creates an icon-only button with an SVG graphic.
     */
    private Button createActionButton(String svgContent, String tooltip, javafx.event.EventHandler<javafx.event.ActionEvent> handler, String styleClass) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().add("svg-path");

        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().add("icon-button");
        if (styleClass != null && !styleClass.isEmpty()) {
            button.getStyleClass().add(styleClass);
        }
        button.setTooltip(new Tooltip(tooltip));

        if (handler != null) {
            button.setOnAction(handler);
        }

        return button;
    }

    /**
     * Creates the folder icon for the set.
     */
    private SVGPath createSetIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 7a2 2 0 012-2h4l2 2h4a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V7z");
        return icon;
    }

    /**
     * Expands or collapses the note panel.
     */
    private void toggleExpand() {
        isExpanded = !isExpanded;

        RotateTransition rotateTransition = new RotateTransition(Duration.millis(200), arrowIcon);
        rotateTransition.setToAngle(isExpanded ? 90 : 0);
        rotateTransition.play();

        notesPanel.setVisible(isExpanded);
        notesPanel.setManaged(isExpanded);

        if (isExpanded) {
            refreshNotesList();
        }
    }

    /**
     * Adds a new note to the set.
     */
    private void addNewNote() {
        if (!isExpanded) {
            toggleExpand();
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Note");
        dialog.setHeaderText("Enter title for note in \"" + noteSet.getName() + "\":");
        dialog.setContentText("Title:");
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
     * Deletes the set and all associated notes.
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
     * Renames the set.
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
     * Refreshes the list of notes when the panel is expanded.
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
