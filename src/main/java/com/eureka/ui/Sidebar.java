package com.eureka.ui;

import com.eureka.I18n;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import java.util.Optional;
import java.util.List;

/**
 * Represents the collapsible sidebar component of the application.
 * Displays a list of note sets (SetRow instances) and provides controls
 * for creating new sets and collapsing/expanding the sidebar itself.
 */
public class Sidebar extends BorderPane {

    // VBox container holding all the SetRow components.
    private final VBox setsPanel;
    // Reference to the global application state.
    private final AppState appState;
    // Listener to notify about note selection events originating from this sidebar.
    private final NoteSelectionListener noteSelectionListener;
    // Reference to the parent SplitPane containing this sidebar and the editor.
    private final SplitPane parentSplitPane;
    // Button for creating a new note set.
    private final Button newSetButton;
    // ScrollPane that contains the setsPanel, allowing scrolling if sets exceed available height.
    private final ScrollPane scrollPane;
    // Button to toggle the collapsed/expanded state of the sidebar.
    private final Button toggleButton;
    // SVG icon used within the toggleButton, animates rotation.
    private final SVGPath toggleIcon;

    private boolean isCollapsed = false;
    private double lastDividerPosition = 0.3; // Default expanded position

    /**
     * Constructs the Sidebar.
     * @param listener        The listener to notify when notes are selected.
     * @param parentSplitPane The SplitPane that contains this Sidebar, used for collapse/expand animations.
     */
    public Sidebar(NoteSelectionListener listener, SplitPane parentSplitPane) {
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.parentSplitPane = parentSplitPane;

        this.getStyleClass().add("sidebar");
        this.setMinWidth(52);
        this.setPrefWidth(280);

        // --- Toggle Button Setup ---
        toggleButton = new Button();
        toggleIcon = new SVGPath();
        toggleIcon.getStyleClass().add("sidebar-toggle-icon");
        toggleIcon.setContent("M 10 4 L 4 10 L 10 16");
        toggleButton.setGraphic(toggleIcon);
        toggleButton.getStyleClass().add("sidebar-toggle-button");
        toggleButton.setOnAction(e -> toggleCollapse(true));

        // --- New Set Button Setup ---
        newSetButton = new Button();
        newSetButton.textProperty().bind(I18n.bind("button.newSet"));
        newSetButton.getStyleClass().add("new-set-button");
        newSetButton.setMaxWidth(Double.MAX_VALUE);
        newSetButton.setOnAction(e -> createNewSet());

        // --- Top Bar Layout (contains New Set and Toggle buttons) ---
        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(12, 0, 0, 12));
        topBar.setCenter(newSetButton);
        topBar.setRight(toggleButton);
        BorderPane.setMargin(toggleButton, new Insets(0, 12, 0, 8));

        this.setTop(topBar);

        // --- Sets Panel Setup (VBox inside ScrollPane) ---
        setsPanel = new VBox(8);
        setsPanel.setPadding(new Insets(12, 12, 0, 12));

        // ScrollPane to contain the setsPanel
        scrollPane = new ScrollPane(setsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("sidebar-scroll-pane");

        this.setCenter(scrollPane);
        updateSetsList();
    }

    /**
     * Toggles the collapsed/expanded state of the sidebar.
     * Saves the state, animates the toggle button, and calls collapse or expand.
     * @param animate true to animate the transition, false for immediate change.
     */
    private void toggleCollapse(boolean animate) {
        isCollapsed = !isCollapsed;
        appState.setSidebarCollapsed(isCollapsed);

        animateToggleButton();

        if (isCollapsed) {
            if (parentSplitPane.getDividers().get(0).getPosition() > 0.01) {
                lastDividerPosition = parentSplitPane.getDividers().get(0).getPosition();
            }
            collapse(animate);
        } else {
            expand(animate);
        }
    }

    /**
     * Collapses the sidebar.
     * Hides and removes main content from layout, animates the divider to position 0.
     * @param animate true to animate the divider transition.
     */
    public void collapse(boolean animate) {
        isCollapsed = true;
        animateToggleButton();

        newSetButton.setManaged(false);
        newSetButton.setVisible(false);
        scrollPane.setManaged(false);
        scrollPane.setVisible(false);

        if (animate) {
            animateDividerTo(0.0);
        } else {
            parentSplitPane.setDividerPosition(0, 0.0);
        }
    }

    /**
     * Expands the sidebar.
     * Shows and includes main content in layout, animates the divider to its last known position.
     * @param animate true to animate the divider transition.
     */
    public void expand(boolean animate) {
        isCollapsed = false;
        animateToggleButton();

        newSetButton.setManaged(true);
        newSetButton.setVisible(true);
        scrollPane.setManaged(true);
        scrollPane.setVisible(true);

        if (animate) {
            animateDividerTo(lastDividerPosition);
        } else {
            parentSplitPane.setDividerPosition(0, lastDividerPosition);
        }
    }

    /**
     * Animates the SplitPane divider to a target position.
     * @param targetPosition The target position for the divider (between 0.0 and 1.0).
     */
    private void animateDividerTo(double targetPosition) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(parentSplitPane.getDividers().get(0).positionProperty(), targetPosition);
        KeyFrame kf = new KeyFrame(Duration.millis(200), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    /**
     * Animates the rotation of the toggle button's arrow icon.
     * Rotates to 180 degrees when collapsed ('>') and 0 degrees when expanded ('<').
     */
    private void animateToggleButton() {
        RotateTransition rt = new RotateTransition(Duration.millis(200), toggleIcon);
        rt.setToAngle(isCollapsed ? 180 : 0);
        rt.play();
    }


    /**
     * Opens a dialog to prompt the user for a new set name.
     * If a valid name is entered, creates a new NoteSet, adds it to the AppState,
     * and updates the list displayed in the sidebar.
     */
    private void createNewSet() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.titleProperty().bind(I18n.bind("dialog.newSet.title"));
        dialog.headerTextProperty().bind(I18n.bind("dialog.newSet.header"));
        dialog.contentTextProperty().bind(I18n.bind("dialog.newSet.contentText"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty()) {
                NoteSet newSet = new NoteSet(trimmedName);
                appState.addSet(newSet);
                updateSetsList();
            }
        });
    }

    /**
     * Clears and repopulates the setsPanel with SetRow components
     * based on the current list of sets in AppState.
     */
    public void updateSetsList() {
        setsPanel.getChildren().clear();
        for (NoteSet set : appState.getSets()) {
            SetRow setRow = new SetRow(set, noteSelectionListener, this::updateSetsList);
            setsPanel.getChildren().add(setRow);
        }
    }

    /**
     * Ensures the SetRow corresponding to the given note's set ID is expanded.
     * If the sidebar is collapsed, it expands it first.
     * @param note The Note whose parent SetRow should be expanded. Can be null.
     */
    public void expandSetForNote(Note note) {
        if (isCollapsed) {
            expand(true);
        }
        if (note == null) return;

        String setId = note.getSetId();
        for (Node child : setsPanel.getChildren()) {
            if (child instanceof SetRow setRow) {
                if (setRow.getNoteSet().getId().equals(setId)) {
                    setRow.expand();
                    break;
                }
            }
        }
    }

    /**
     * Updates the highlighting state of all NoteRows within all SetRows.
     * Only the NoteRow corresponding to the activeNote (if any) will be highlighted.
     * @param activeNote The Note that is currently active/selected in the editor, or null if none.
     */
    public void updateNoteHighlighting(Note activeNote) {
        for (Node child : setsPanel.getChildren()) {
            if (child instanceof SetRow setRow) {
                for (NoteRow noteRow : setRow.getNoteRows()) {
                    boolean isActive = activeNote != null && noteRow.getNote().getId().equals(activeNote.getId());
                    noteRow.setActive(isActive);
                }
            }
        }
    }
}