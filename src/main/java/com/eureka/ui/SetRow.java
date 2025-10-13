package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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

        // --- Header Panel ---
        HBox headerPanel = new HBox(4);
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.getStyleClass().add("header");

        arrowIcon = new SVGPath();
        arrowIcon.setContent("M8 5l6 6-6 6z"); // Right-pointing arrow

        // Use a Region to hold the SVG shape for better styling control
        Region arrowContainer = new Region();
        arrowContainer.setShape(arrowIcon);
        arrowContainer.getStyleClass().add("arrow-icon");

        this.setNameLabel = new Label(noteSet.getName());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- Icon Buttons ---
        Button addButton = createIconButton("M12 5v14m-7-7h14"); // Plus Icon
        addButton.setOnAction(e -> addNewNote());

        Button menuButton = createIconButton("M12 8a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4z"); // Vertical dots icon

        // --- Context Menu for Actions ---
        ContextMenu contextMenu = new ContextMenu();
        MenuItem renameItem = new MenuItem("Rename Set");
        renameItem.setOnAction(e -> renameSet());
        MenuItem deleteItem = new MenuItem("Delete Set");
        deleteItem.getStyleClass().add("destructive-menu-item");
        deleteItem.setOnAction(e -> deleteSet());
        contextMenu.getItems().addAll(renameItem, new SeparatorMenuItem(), deleteItem);

        menuButton.setOnAction(e -> contextMenu.show(menuButton, Side.BOTTOM, 0, 5));

        headerPanel.getChildren().addAll(arrowContainer, setNameLabel, spacer, addButton, menuButton);

        // --- Notes Panel ---
        notesPanel = new VBox(0); // Spacing is handled by CSS
        notesPanel.getStyleClass().add("notes-panel");
        notesPanel.setVisible(false);
        notesPanel.setManaged(false);

        this.getChildren().addAll(headerPanel, notesPanel);

        // --- Event Handlers ---
        headerPanel.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Button || event.getTarget() instanceof SVGPath)) {
                toggleExpand();
            }
        });
    }

    private Button createIconButton(String svgContent) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().add("svg-path");
        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().add("icon-button");
        return button;
    }

    private void toggleExpand() {
        isExpanded = !isExpanded;
        RotateTransition rt = new RotateTransition(Duration.millis(200), arrowIcon);
        rt.setToAngle(isExpanded ? 90 : 0);
        rt.play();

        notesPanel.setVisible(isExpanded);
        notesPanel.setManaged(isExpanded);
        if (isExpanded) {
            refreshNotesList();
        }
    }

    private void addNewNote() {
        if (!isExpanded) {
            toggleExpand();
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Note");
        dialog.setHeaderText("Enter title for note in \"" + noteSet.getName() + "\":");
        dialog.setContentText("Title:");
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

    private void deleteSet() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Set");
        alert.setHeaderText("Delete the set \"" + noteSet.getName() + "\"?");
        alert.setContentText("This will permanently delete the set and all notes within it.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            List<Note> notesToDelete = List.copyOf(appState.getNotesForSet(noteSet.getId()));
            notesToDelete.forEach(note -> EurekaApp.getSearchService().deleteNote(note));
            appState.deleteSet(noteSet.getId());
            noteSelectionListener.onSetDeleted(noteSet.getId(), notesToDelete);
            onSetChangedCallback.run();
        }
    }

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

    // --- METHODS THAT WERE MISSING ---
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