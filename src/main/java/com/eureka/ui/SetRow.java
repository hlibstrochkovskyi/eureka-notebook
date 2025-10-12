package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

public class SetRow extends VBox {

    private final NoteSet noteSet;
    private final VBox notesPanel;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private boolean isExpanded = false;
    private final Runnable onSetDeletedCallback;

    // We make the label a field of the class to easily access it from any method
    private final Label setNameLabel;

    public SetRow(NoteSet noteSet, NoteSelectionListener listener, Runnable onSetDeletedCallback) {
        this.noteSet = noteSet;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onSetDeletedCallback = onSetDeletedCallback;

        HBox headerPanel = new HBox();
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.setPadding(new Insets(4, 8, 4, 12));
        headerPanel.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 5; -fx-cursor: hand;");

        // Initialize the field instead of a local variable
        this.setNameLabel = new Label("▶ " + noteSet.getName());
        this.setNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        this.setNameLabel.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+");
        addButton.setStyle("-fx-cursor: default;");
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-cursor: default;");
        HBox buttonsPanel = new HBox(4, addButton, deleteButton);

        headerPanel.getChildren().addAll(setNameLabel, spacer, buttonsPanel);

        notesPanel = new VBox(4);
        notesPanel.setPadding(new Insets(8, 0, 0, 15));
        notesPanel.setVisible(false);
        notesPanel.setManaged(false);

        this.getChildren().addAll(headerPanel, notesPanel);

        headerPanel.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Button)) {
                toggleExpand();
            }
        });
        addButton.setOnAction(event -> addNewNote());
        deleteButton.setOnAction(event -> deleteSet());
    }

    private void toggleExpand() {
        isExpanded = !isExpanded;
        // Now we can directly access the field
        setNameLabel.setText((isExpanded ? "▼ " : "▶ ") + noteSet.getName());
        notesPanel.setVisible(isExpanded);
        notesPanel.setManaged(isExpanded);
        if (isExpanded) {
            refreshNotesList();
        }
    }

    private void addNewNote() {
        // THE FIX: No more casting! We just use the field directly.
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
            appState.deleteSet(noteSet.getId());
            noteSelectionListener.onSetDeleted(noteSet.getId(), notesToDelete);
            onSetDeletedCallback.run();
        }
    }

    private void refreshNotesList() {
        notesPanel.getChildren().clear();
        List<Note> notesInSet = appState.getNotesForSet(noteSet.getId());

        if (notesInSet.isEmpty()) {
            Label emptyLabel = new Label("This set is empty.");
            emptyLabel.setStyle("-fx-text-fill: grey; -fx-padding: 5;");
            notesPanel.getChildren().add(emptyLabel);
        } else {
            for (Note note : notesInSet) {
                NoteRow noteRow = new NoteRow(note, noteSelectionListener, this::refreshNotesList);
                notesPanel.getChildren().add(noteRow);
            }
        }
    }

    public List<NoteRow> getNoteRows() {
        return notesPanel.getChildren().stream()
                .filter(node -> node instanceof NoteRow)
                .map(node -> (NoteRow) node)
                .toList();
    }
}