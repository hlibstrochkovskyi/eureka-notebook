package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a collapsible row in the sidebar displaying a NoteSet (folder).
 * Contains a header with the set's name, icons, action buttons (add note, context menu),
 * and a collapsible panel (VBox) to display the NoteRows belonging to this set.
 */
public class SetRow extends VBox {

    // The NoteSet data object this row represents.
    private final NoteSet noteSet;
    // The VBox panel that holds the NoteRows for this set (collapsible).
    private final VBox notesPanel;
    // Listener to notify about note interactions within this set.
    private final NoteSelectionListener noteSelectionListener;
    // Reference to the application's global state.
    private final AppState appState;
    // Tracks whether the notesPanel is currently expanded.
    private boolean isExpanded = false;
    // Callback function to run when changes require the parent (Sidebar) to refresh.
    private final Runnable onSetChangedCallback;
    // Label displaying the name of the note set.
    private final Label setNameLabel;
    // SVG icon used as the expand/collapse arrow.
    private final SVGPath arrowIcon;
    // SVG icon representing the set (folder).
    private final SVGPath setIcon;

    /**
     * Constructs a SetRow UI component.
     * @param noteSet               The NoteSet object to display.
     * @param listener              The listener to notify about note interactions.
     * @param onSetChangedCallback A callback function to execute when the set list needs refreshing
     * (e.g., after deleting or renaming this set).
     */
    public SetRow(NoteSet noteSet, NoteSelectionListener listener, Runnable onSetChangedCallback) {
        this.noteSet = noteSet;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.onSetChangedCallback = onSetChangedCallback;

        getStyleClass().add("set-row");

        // === Header Panel (HBox containing icons, label, buttons) ===
        HBox headerPanel = new HBox(8);
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        headerPanel.getStyleClass().add("header");
        headerPanel.setPadding(new Insets(4, 0, 4, 0));

        // --- Expand/Collapse Arrow ---
        arrowIcon = new SVGPath();
        arrowIcon.setContent("M8 5l6 6-6 6z");
        arrowIcon.getStyleClass().add("arrow-icon");

        // Container for the arrow icon
        Region arrowContainer = new Region();
        arrowContainer.setShape(arrowIcon);
        arrowContainer.getStyleClass().add("arrow-icon");
        arrowContainer.setMinWidth(20);
        arrowContainer.setMinHeight(20);

        // --- Set Icon (folder) ---
        setIcon = createSetIcon();
        Region setIconContainer = new Region();
        setIconContainer.setShape(setIcon);
        setIconContainer.getStyleClass().add("set-icon-container");
        setIconContainer.setMinWidth(18);
        setIconContainer.setMinHeight(18);

        // --- Set Name Label ---
        this.setNameLabel = new Label(noteSet.getName());
        setNameLabel.setStyle("-fx-font-weight: semibold; -fx-font-size: 13;"); // Styling

        // --- Spacer (pushes action buttons to the right) ---
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // Allow spacer to grow horizontally

        // === Action Buttons ===
        // --- Add Note Button ---
        Button addButton = createActionButton(
                "M12 5v14m-7-7h14",  // Plus icon SVG
                "Add new note",      // Tooltip text
                e -> addNewNote()      // Action handler
        );

        // --- Context Menu Button ---
        Button menuButton = createActionButton(
                "M12 8a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4zm0 6a2 2 0 110-4 2 2 0 010 4z",  // Vertical dots SVG
                "More options",       // Tooltip text
                null                  // Action handler set below to show ContextMenu
        );

        // === Context Menu (for Rename/Delete Set) ===
        ContextMenu contextMenu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Rename Set");
        renameItem.setOnAction(e -> renameSet()); // Link to renameSet method

        MenuItem deleteItem = new MenuItem("Delete Set");
        deleteItem.getStyleClass().add("destructive-menu-item"); // Style for dangerous actions
        deleteItem.setOnAction(e -> deleteSet()); // Link to deleteSet method

        contextMenu.getItems().addAll(renameItem, new SeparatorMenuItem(), deleteItem); // Add items
        // Make the menuButton show the contextMenu when clicked
        menuButton.setOnAction(e -> contextMenu.show(menuButton, Side.BOTTOM, 0, 5));

        // --- Add components to the header ---
        headerPanel.getChildren().addAll(
                arrowContainer, setIconContainer, setNameLabel, spacer, addButton, menuButton
        );

        // === Notes Panel (collapsible VBox for NoteRows) ===
        notesPanel = new VBox(0);
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
     * Creates an action button with an SVG icon and tooltip.
     *
     * @param svgContent The string containing the SVG path data for the icon.
     * @param tooltip    The text to display in the button's tooltip.
     * @param handler    The EventHandler to be called when the button is clicked. Can be null.
     * @return A configured Button.
     */
    private Button createActionButton(String svgContent, String tooltip, javafx.event.EventHandler<ActionEvent> handler) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.getStyleClass().add("svg-path");

        Button button = new Button();
        button.setGraphic(path);
        button.getStyleClass().add("icon-button");
        button.setTooltip(new Tooltip(tooltip));

        if (handler != null) {
            button.setOnAction(handler);
        }

        return button;
    }

    /**
     * Creates the SVGPath object representing the folder icon for the set.
     * @return An SVGPath object configured as a folder icon.
     */
    private SVGPath createSetIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 7a2 2 0 012-2h4l2 2h4a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V7z");
        icon.getStyleClass().add("svg-path");
        return icon;
    }

    /**
     * Toggles the visibility and managed state of the notesPanel.
     * Animates the expand/collapse arrow icon.
     * Refreshes the list of notes if expanding.
     */
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

    /**
     * Opens a dialog to add a new note to the current set.
     * Expands the set if it's currently collapsed.
     * Creates the note, adds it to the AppState and search service,
     * refreshes the notes list, and selects the new note.
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
            String trimmedTitle = title.trim();
            if (!trimmedTitle.isEmpty()) {
                Note newNote = new Note(noteSet.getId(), trimmedTitle);
                appState.addNote(newNote);
                EurekaApp.getSearchService().addOrUpdateNote(newNote);
                refreshNotesList();
                noteSelectionListener.onNoteSelected(newNote);
            }
        });
    }

    /**
     * Shows a confirmation dialog and deletes the entire set and all notes within it if confirmed.
     * Removes notes from the search service, updates AppState, notifies listeners,
     * and triggers the callback to refresh the parent list (Sidebar).
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
     * Opens a dialog to rename the current set.
     * If a new valid name is entered, it updates the NoteSet object,
     * updates the UI label, and triggers the callback to refresh the parent list.
     */
    private void renameSet() {
        TextInputDialog dialog = new TextInputDialog(noteSet.getName());
        dialog.setTitle("Rename Set");
        dialog.setHeaderText("Enter new name for the set:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            String trimmedName = newName.trim();
            if (!trimmedName.isEmpty() && !trimmedName.equals(noteSet.getName())) {
                noteSet.setName(trimmedName);
                this.setNameLabel.setText(noteSet.getName());
                onSetChangedCallback.run();
            }
        });
    }

    /**
     * Clears and repopulates the notesPanel with NoteRow instances
     * corresponding to the notes currently in this set according to AppState.
     * Displays a label if the set contains no notes.
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

    /**
     * Gets the NoteSet object associated with this row.
     * @return The NoteSet object.
     */
    public NoteSet getNoteSet() {
        return noteSet;
    }

    /**
     * Programmatically expands the notes panel if it is currently collapsed.
     * Calls toggleExpand() only if needed.
     */
    public void expand() {
        if (!isExpanded) {
            toggleExpand();
        }
    }

    /**
     * Gets a list of the NoteRow components currently displayed within the notesPanel.
     * @return A List of NoteRow objects.
     */
    public List<NoteRow> getNoteRows() {
        return notesPanel.getChildren().stream()
                .filter(node -> node instanceof NoteRow)
                .map(node -> (NoteRow) node)
                .collect(Collectors.toList());
    }
}