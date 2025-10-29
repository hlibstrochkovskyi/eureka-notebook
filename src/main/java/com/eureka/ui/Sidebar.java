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

public class Sidebar extends BorderPane {

    private final VBox setsPanel;
    private final AppState appState;
    private final NoteSelectionListener noteSelectionListener;
    private final SplitPane parentSplitPane;
    private final Button newSetButton;
    private final ScrollPane scrollPane;
    private final Button toggleButton;
    private final SVGPath toggleIcon;

    private boolean isCollapsed = false;
    private double lastDividerPosition = 0.3;

    public Sidebar(NoteSelectionListener listener, SplitPane parentSplitPane) {
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.parentSplitPane = parentSplitPane;

        this.getStyleClass().add("sidebar");
        this.setMinWidth(52);
        this.setPrefWidth(280);

        toggleButton = new Button();
        toggleIcon = new SVGPath();
        toggleIcon.getStyleClass().add("sidebar-toggle-icon");
        toggleIcon.setContent("M 10 4 L 4 10 L 10 16");
        toggleButton.setGraphic(toggleIcon);
        toggleButton.getStyleClass().add("sidebar-toggle-button");
        toggleButton.setOnAction(e -> toggleCollapse(true));

        newSetButton = new Button();
        newSetButton.textProperty().bind(I18n.bind("button.newSet"));
        newSetButton.getStyleClass().add("new-set-button");
        newSetButton.setMaxWidth(Double.MAX_VALUE);
        newSetButton.setOnAction(e -> createNewSet());

        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(12, 0, 0, 12));
        topBar.setCenter(newSetButton);
        topBar.setRight(toggleButton);
        BorderPane.setMargin(toggleButton, new Insets(0, 12, 0, 8));

        this.setTop(topBar);

        setsPanel = new VBox(8);
        setsPanel.setPadding(new Insets(12, 12, 0, 12));

        scrollPane = new ScrollPane(setsPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("sidebar-scroll-pane");

        this.setCenter(scrollPane);
        updateSetsList();
    }

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

    private void animateDividerTo(double targetPosition) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(parentSplitPane.getDividers().get(0).positionProperty(), targetPosition);
        KeyFrame kf = new KeyFrame(Duration.millis(200), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    private void animateToggleButton() {
        RotateTransition rt = new RotateTransition(Duration.millis(200), toggleIcon);
        rt.setToAngle(isCollapsed ? 180 : 0); // 0 degrees for '<', 180 for '>'
        rt.play();
    }


    private void createNewSet() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.titleProperty().bind(I18n.bind("dialog.newSet.title"));
        dialog.headerTextProperty().bind(I18n.bind("dialog.newSet.header"));
        dialog.contentTextProperty().bind(I18n.bind("dialog.newSet.contentText"));

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