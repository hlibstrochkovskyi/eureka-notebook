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

    // A callback to notify the parent (Sidebar) that it needs to refresh.
    private final Runnable onSetDeletedCallback;

    public SetRow(NoteSet noteSet, NoteSelectionListener listener, Runnable onSetDeletedCallback) {
        this.noteSet = noteSet;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onSetDeletedCallback = onSetDeletedCallback;

        // Header for the set (name and buttons)
        BorderPane headerPanel = new BorderPane();
        headerPanel.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 5;");
        headerPanel.setPadding(new Insets(4, 8, 4, 12));

        Label setNameLabel = new Label(noteSet.getName());
        setNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Buttons (+ and x)
        Button addButton = new Button("+");
        Button deleteButton = new Button("×"); // A better-looking 'x'
        HBox buttonsPanel = new HBox(4, addButton, deleteButton);
        buttonsPanel.setAlignment(Pos.CENTER);

        headerPanel.setLeft(setNameLabel);
        headerPanel.setRight(buttonsPanel);

        // This panel will hold the list of notes for this set
        notesPanel = new VBox(4);
        notesPanel.setPadding(new Insets(8, 0, 0, 15));
        notesPanel.setVisible(false); // Initially hidden
        notesPanel.setManaged(false); // Don't take up space when hidden

        // Add header and notes panel to the main VBox
        this.getChildren().addAll(headerPanel, notesPanel);

        // --- Event Handlers ---

        // Click on the header to expand/collapse
        headerPanel.setOnMouseClicked(event -> toggleExpand());

        // Click on the '+' button to add a new note
        addButton.setOnAction(event -> addNewNote());

        // Click on the 'x' button to delete the set
        deleteButton.setOnAction(event -> deleteSet());
    }

    /**
     * Toggles the visibility of the notes list panel.
     */
    private void toggleExpand() {
        isExpanded = !isExpanded;
        notesPanel.setVisible(isExpanded);
        notesPanel.setManaged(isExpanded); // Manage layout space
        if (isExpanded) {
            refreshNotesList();
        }
    }

    /**
     * Opens a dialog to create a new note within this set.
     */
    private void addNewNote() {
        if (!isExpanded) {
            toggleExpand();
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Note");
        dialog.setHeaderText("Enter title for the new note in \"" + noteSet.getName() + "\":");
        dialog.setContentText("Title:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(title -> {
            if (!title.trim().isEmpty()) {
                Note newNote = new Note(noteSet.getId(), title.trim());
                appState.addNote(newNote);
                refreshNotesList();
                noteSelectionListener.onNoteSelected(newNote); // Open the new note
            }
        });
    }

    /**
     * Opens a confirmation dialog and deletes the entire set if confirmed.
     */
    private void deleteSet() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Set");
        alert.setHeaderText("Delete the set \"" + noteSet.getName() + "\"?");
        alert.setContentText("Are you sure? This will permanently delete the set and all notes within it.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            appState.deleteSet(noteSet.getId());

            // Notify the EditorContainer to close any tabs from this set
            noteSelectionListener.onSetDeleted(noteSet.getId());

            // Notify the Sidebar to refresh its list
            onSetDeletedCallback.run();
        }
    }

    /**
     * Clears and re-populates the list of notes for this set.
     */
    private void refreshNotesList() {
        notesPanel.getChildren().clear();
        List<Note> notesInSet = appState.getNotesForSet(noteSet.getId());
        for (Note note : notesInSet) {
            // Now we use our new JavaFX NoteRow
            NoteRow noteRow = new NoteRow(note, noteSelectionListener, this::refreshNotesList);
            notesPanel.getChildren().add(noteRow);
        }
    }
    }
