package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class Sidebar extends BorderPane {

    private final VBox setsPanel;
    private final AppState appState;
    private final NoteSelectionListener noteSelectionListener;

    public Sidebar(NoteSelectionListener listener) {
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();

        // main panel styles
        this.setPadding(new Insets(12));
        this.setStyle("-fx-background-color: #f4f4f5; -fx-border-width: 0 1 0 0; -fx-border-color: #e5e7eb;");
        this.setPrefWidth(280);

        // 1. button to create new set
        Button newSetButton = new Button("New Set");
        newSetButton.setMaxWidth(Double.MAX_VALUE); // to stretch button
        newSetButton.setStyle("-fx-background-color: #e11d48; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        newSetButton.setOnAction(e -> createNewSet()); // action on click

        this.setTop(newSetButton); // placing button on top

        // 2. Panel for the list of sets with scrolling
        setsPanel = new VBox(8); // VBox with 8px spacing
        setsPanel.setPadding(new Insets(12, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(setsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        this.setCenter(scrollPane);

        // Initial display of all sets
        updateSetsList();
    }


    /**
     * Opens a dialog window to create a new set.
     */
    private void createNewSet() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Set");
        dialog.setHeaderText("Enter the name for the new set:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                NoteSet newSet = new NoteSet(name.trim());
                appState.addSet(newSet);
                updateSetsList(); // Update the UI
            }
        });
    }

    /**
     * Clears and re-populates the list of sets in the sidebar UI.
     * This is the single, correct version of the method.
     */
    public void updateSetsList() {
        setsPanel.getChildren().clear(); // Clear the old list
        for (NoteSet set : appState.getSets()) {
            // Now, we create and add the REAL, interactive SetRow component!
            SetRow setRow = new SetRow(set, noteSelectionListener, this::updateSetsList);
            setsPanel.getChildren().add(setRow);
        }
    }

    /**
     * Updates the highlighting of note rows based on the currently active note.
     */
    public void updateNoteHighlighting(Note activeNote) {
        for (var child : setsPanel.getChildren()) {
            if (child instanceof SetRow setRow) {
                for (NoteRow noteRow : setRow.getNoteRows()) {
                    boolean isActive = activeNote != null && noteRow.getNote().getId().equals(activeNote.getId());
                    noteRow.setActive(isActive);
                }
            }
        }
    }
}