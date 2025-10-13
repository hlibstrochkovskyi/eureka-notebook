package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.Optional;

public class Sidebar extends BorderPane {

    private final VBox setsPanel;
    private final AppState appState;
    private final NoteSelectionListener noteSelectionListener;
    private final SplitPane parentSplitPane;
    private final Button newSetButton;
    private final ScrollPane scrollPane;
    private final Button toggleButton;

    private boolean isCollapsed = false;
    private double lastDividerPosition = 0.3; // Default open position


    public Sidebar(NoteSelectionListener listener, SplitPane parentSplitPane) {
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.parentSplitPane = parentSplitPane;

        // --- Main panel styles ---
        this.getStyleClass().add("sidebar");
        this.setMinWidth(48); // The minimum width when collapsed

        // --- Toggle Button ---
        toggleButton = new Button();
        toggleButton.getStyleClass().add("sidebar-toggle-button");
        toggleButton.setOnAction(e -> toggleCollapse(true));

        // --- New Set Button ---
        newSetButton = new Button("New Set");
        newSetButton.getStyleClass().add("new-set-button");
        newSetButton.setMaxWidth(Double.MAX_VALUE);
        newSetButton.setOnAction(e -> createNewSet());

        // --- Top Bar Container (HBox) ---
        HBox topBar = new HBox();
        topBar.setSpacing(8); // Add some space between the buttons
        topBar.setPadding(new Insets(12, 12, 0, 12));

        // FIX 1: Allow newSetButton to grow and fill space
        HBox.setHgrow(newSetButton, Priority.ALWAYS);

        topBar.getChildren().addAll(newSetButton, toggleButton);
        this.setTop(topBar);

        // --- Sets Panel ---
        setsPanel = new VBox(8);
        setsPanel.setPadding(new Insets(12, 12, 0, 12));

        scrollPane = new ScrollPane(setsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("sidebar-scroll-pane");

        this.setCenter(scrollPane);

        // FIX 2: Set the initial icon for the toggle button
        updateToggleButton();

        updateSetsList();
    }

    private void toggleCollapse(boolean animate) {
        isCollapsed = !isCollapsed;
        appState.setSidebarCollapsed(isCollapsed); // Save state

        if (isCollapsed) {
            // Before collapsing, save the current divider position if it's open
            if (parentSplitPane.getDividers().get(0).getPosition() > 0.01) {
                lastDividerPosition = parentSplitPane.getDividers().get(0).getPosition();
            }
            collapse(animate);
        } else {
            expand(animate);
        }
    }

    public void collapse(boolean animate) {
        isCollapsed = true;
        updateToggleButton();
        newSetButton.setVisible(false);
        scrollPane.setVisible(false);

        if (animate) {
            animateDividerTo(0.0);
        } else {
            parentSplitPane.setDividerPosition(0, 0.0);
        }
    }

    public void expand(boolean animate) {
        isCollapsed = false;
        updateToggleButton();
        newSetButton.setVisible(true);
        scrollPane.setVisible(true);

        if (animate) {
            animateDividerTo(lastDividerPosition);
        } else {
            parentSplitPane.setDividerPosition(0, lastDividerPosition);
        }
    }

    private void animateDividerTo(double targetPosition) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(parentSplitPane.getDividers().get(0).positionProperty(), targetPosition);
        KeyFrame kf = new KeyFrame(Duration.millis(200), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    private void updateToggleButton() {
        // This simple graphic will be styled by CSS to look like < or >
        if (isCollapsed) {
            toggleButton.setText(">");
        } else {
            toggleButton.setText("<");
        }
    }

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
                updateSetsList();
            }
        });
    }

    public void updateSetsList() {
        setsPanel.getChildren().clear();
        for (NoteSet set : appState.getSets()) {
            SetRow setRow = new SetRow(set, noteSelectionListener, this::updateSetsList);
            setsPanel.getChildren().add(setRow);
        }
    }

    public void expandSetForNote(Note note) {
        if (isCollapsed) {
            expand(true);
        }
        if (note == null) return;
        String setId = note.getSetId();
        for (var child : setsPanel.getChildren()) {
            if (child instanceof SetRow setRow) {
                if (setRow.getNoteSet().getId().equals(setId)) {
                    setRow.expand();
                    break;
                }
            }
        }
    }

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