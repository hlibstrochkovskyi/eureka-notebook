package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SetRow renders a single note set inside the sidebar.
 * It shows the set icon, name, action buttons, and an expandable list of notes.
 */
public class SetRow extends VBox {

    private final NoteSet noteSet;
    private final VBox notesPanel;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private final Runnable onSetChangedCallback;
    private final Label setNameLabel;
    private final SVGPath arrowIcon;

    private boolean isExpanded = false;

    public SetRow(NoteSet noteSet, NoteSelectionListener listener, Runnable onSetChangedCallback) {
        this.noteSet = noteSet;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onSetChangedCallback = onSetChangedCallback;

        getStyleClass().add("set-row");

        // --- Header ---
        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.getStyleClass().add("set-header");

        arrowIcon = new SVGPath();
        arrowIcon.setContent("M8 5l6 6-6 6z");
        arrowIcon.getStyleClass().addAll("svg-path", "arrow-icon");

        StackPane arrowToggle = new StackPane(arrowIcon);
        arrowToggle.getStyleClass().add("arrow-toggle");
        arrowToggle.setOnMouseClicked(event -> {
            event.consume();
            toggleExpand();
        });

        SVGPath setGlyph = createSetIcon();
        StackPane setIconContainer = new StackPane(setGlyph);
        setIconContainer.getStyleClass().add("set-icon-wrapper");

        setNameLabel = new Label(noteSet.getName());
        setNameLabel.getStyleClass().add("set-name-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = createActionButton(
                "M12 5v14m-7-7h14",
                "Add new note",
                event -> addNewNote(),
                "add-note-button"
        );

        Button menuButton = createActionButton(
                "M12 8a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4z",
                "More options",
                null,
                "options-button"
        );

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("set-context-menu");

        MenuItem renameItem = new MenuItem("Rename Set");
        renameItem.setOnAction(event -> renameSet());

        MenuItem deleteItem = new MenuItem("Delete Set");
        deleteItem.getStyleClass().add("destructive-menu-item");
        deleteItem.setOnAction(event -> deleteSet());

        contextMenu.getItems().addAll(renameItem, new SeparatorMenuItem(), deleteItem);
        menuButton.setOnMouseClicked(mouseEvent -> {
            mouseEvent.consume();
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            } else {
                contextMenu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 6);
            }
        });

        headerPanel.getChildren().addAll(
                arrowToggle,
                setIconContainer,
                setNameLabel,
                spacer,
                addButton,
                menuButton
        );

        notesPanel = new VBox();
        notesPanel.getStyleClass().add("notes-panel");
        notesPanel.setVisible(false);
        notesPanel.setManaged(false);

        getChildren().addAll(headerPanel, notesPanel);

        headerPanel.setOnMouseClicked(event -> {
            Object target = event.getTarget();
            if (!(target instanceof Button || target instanceof SVGPath)) {
                toggleExpand();
            }
        });
    }

    /**
     * Creates a header action button with an SVG glyph.
     */
    private Button createActionButton(String svgContent, String tooltipText,
                                      EventHandler<ActionEvent> handler, String... extraStyleClasses) {
        SVGPath glyph = new SVGPath();
        glyph.setContent(svgContent);
        glyph.getStyleClass().addAll("svg-path", "icon-button-graphic");

        Button button = new Button();
        button.setGraphic(glyph);
        button.getStyleClass().addAll("icon-button", "set-header-button");
        Arrays.stream(extraStyleClasses).forEach(button.getStyleClass()::add);
        button.setFocusTraversable(false);

        if (tooltipText != null && !tooltipText.isBlank()) {
            button.setTooltip(new Tooltip(tooltipText));
        }

        if (handler != null) {
            button.setOnAction(handler);
        }

        return button;
    }

    /**
     * Returns the SVG glyph representing a set/folder.
     */
    private SVGPath createSetIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 7a2 2 0 012-2h4l2 2h4a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V7z");
        icon.getStyleClass().addAll("svg-path", "set-icon");
        return icon;
    }

    /**
     * Expands or collapses the panel with the set's notes.
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
     * Creates a new note inside the set and selects it.
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
     * Removes the entire set along with its notes after confirmation.
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
     * Renames the set and updates the header label.
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
                setNameLabel.setText(noteSet.getName());
                onSetChangedCallback.run();
            }
        });
    }

    /**
     * Rebuilds the list of note rows inside the set panel.
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
