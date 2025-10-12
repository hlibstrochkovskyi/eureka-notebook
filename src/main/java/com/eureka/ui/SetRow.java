package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.Optional;

/**
 * A JavaFX component that represents a single, expandable row for a NoteSet in the sidebar.
 */
public class SetRow extends VBox {

    private final NoteSet noteSet;
    private final VBox notesPanel;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private boolean isExpanded = false;
    private final Runnable onSetDeletedCallback;

    public SetRow(NoteSet noteSet, NoteSelectionListener listener, Runnable onSetDeletedCallback) {
        this.noteSet = noteSet;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onSetDeletedCallback = onSetDeletedCallback;

        // Header for the set (name and buttons)
        BorderPane headerPanel = new BorderPane();
        headerPanel.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 5; -fx-cursor: hand;");
        headerPanel.setPadding(new Insets(4, 8, 4, 12));

        Label setNameLabel = new Label("▶ " + noteSet.getName());
        setNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button addButton = new Button("+");
        addButton.setStyle("-fx-cursor: default;");
        Button deleteButton = new Button("×");
        deleteButton.setStyle("-fx-cursor: default;");
        HBox buttonsPanel = new HBox(4, addButton, deleteButton);
        buttonsPanel.setAlignment(Pos.CENTER);

        headerPanel.setLeft(setNameLabel);
        headerPanel.setRight(buttonsPanel);

        // This panel will hold the list of notes for this set
        notesPanel = new VBox(4);
        notesPanel.setPadding(new Insets(8, 0, 0, 15));
        notesPanel.setVisible(false);
        notesPanel.setManaged(false);

        this.getChildren().addAll(headerPanel, notesPanel);

        // --- Event Handlers ---
        headerPanel.setOnMouseClicked(event -> {
            // Prevent expand/collapse when clicking on buttons
            if (event.getTarget() != addButton && event.getTarget() != deleteButton) {
                toggleExpand(setNameLabel);
            }
        });
        addButton.setOnAction(event -> addNewNote());
        deleteButton.setOnAction(event -> deleteSet());
    }

    private void toggleExpand(Label label) {
        isExpanded = !isExpanded;
        label.setText((isExpanded ? "▼ " : "▶ ") + noteSet.getName()); // Change arrow
        notesPanel.setVisible(isExpanded);
        notesPanel.setManaged(isExpanded);
        if (isExpanded) {
            refreshNotesList();
        }
    }

    private void addNewNote() {
        // This is a bit of a workaround to get the label to update if the panel is closed
        Label label = (Label)((BorderPane)this.getChildren().get(0)).getLeft();
        if (!isExpanded) {
            toggleExpand(label);
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
            // Important: We need a copy because the original list will be modified
            List<Note> notesToDelete = List.copyOf(appState.getNotesForSet(noteSet.getId()));

            appState.deleteSet(noteSet.getId());
            noteSelectionListener.onSetDeleted(noteSet.getId(), notesToDelete);
            onSetDeletedCallback.run();
        }
    }

    /**
     * Clears and re-populates the list of notes for this set.
     * If there are no notes, it displays a message.
     */
    private void refreshNotesList() {
        notesPanel.getChildren().clear();
        List<Note> notesInSet = appState.getNotesForSet(noteSet.getId());

        if (notesInSet.isEmpty()) {
            // THIS IS THE FIX: Show a message if the set is empty
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
    // Method to get all NoteRow children
    public List<NoteRow> getNoteRows() {
        return notesPanel.getChildren().stream()
                .filter(node -> node instanceof NoteRow)
                .map(node -> (NoteRow) node)
                .toList();
    }
}
