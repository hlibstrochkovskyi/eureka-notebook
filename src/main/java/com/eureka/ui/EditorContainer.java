package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages the main editing area, displaying notes in tabs.
 * Implements NoteSelectionListener to react to note selection, deletion, and renaming events.
 */
public class EditorContainer extends BorderPane implements NoteSelectionListener {

    private final TabPane tabPane;
    private final Map<String, Tab> openTabsMap;
    private Sidebar sidebar;

    /**
     * Constructs the EditorContainer.
     * Initializes the TabPane and the map for tracking open tabs.
     * Sets up listeners to manage the openTabsMap when tabs are closed
     * and to update sidebar highlighting when the selected tab changes.
     */
    public EditorContainer() {
        this.openTabsMap = new HashMap<>();
        this.tabPane = new TabPane();
        this.setCenter(tabPane);

        tabPane.getTabs().addListener((ListChangeListener<Tab>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(tab -> openTabsMap.remove(tab.getId()));
                }
            }
            updateSidebarHighlighting();
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateSidebarHighlighting();
        });
    }

    /**
     * Sets the reference to the Sidebar.
     * This is needed to communicate back to the sidebar (e.g., for highlighting).
     * @param sidebar The Sidebar instance.
     */
    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

    /**
     * Handles the selection of a note (e.g., from the sidebar).
     * If the note is already open in a tab, selects that tab.
     * Otherwise, creates a new tab for the selected note.
     * Also ensures the corresponding set in the sidebar is expanded.
     * @param note The Note that was selected. Can be null.
     */
    @Override
    public void onNoteSelected(Note note) {
        if (note == null) return;
        if (sidebar != null) {
            sidebar.expandSetForNote(note);
        }

        if (openTabsMap.containsKey(note.getId())) {
            tabPane.getSelectionModel().select(openTabsMap.get(note.getId()));
        } else {
            createNewTab(note);
        }
    }

    /**
     * Handles note selection specifically coming from a search result.
     * Opens or selects the note's tab and then attempts to highlight
     * the searched text within the editor area.
     * @param note     The Note selected from search results.
     * @param position The starting character index of the search query in the note's content.
     * @param wordIndex (Currently unused in this implementation, but part of the interface).
     * @param query    The search query string that was found.
     */
    @Override
    public void onNoteSelectedFromSearch(Note note, int position, int wordIndex, String query) {
        onNoteSelected(note);
        tabPane.requestFocus();

        Platform.runLater(() -> {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null && selectedTab.getContent() instanceof TextArea editorArea) {
                editorArea.requestFocus();
                editorArea.selectRange(position, position + query.length());
            }
        });
    }

    /**
     * Creates a new Tab for the given Note, adds a TextArea for editing,
     * and sets up a listener to save changes automatically.
     * Adds the new tab to the TabPane and selects it.
     * @param note The Note for which to create a new tab.
     */
    private void createNewTab(Note note) {
        Tab tab = new Tab(note.getTitle());
        tab.setId(note.getId());

        TextArea editorArea = new TextArea(note.getContent());
        editorArea.setWrapText(true);

        editorArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!Objects.equals(note.getContent(), newText)) {
                note.setContent(newText);
                note.setUpdatedAt(System.currentTimeMillis());
                EurekaApp.getSearchService().addOrUpdateNote(note);
            }
        });

        tab.setContent(editorArea);

        openTabsMap.put(note.getId(), tab);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    /**
     * Handles the deletion of a note.
     * Removes the corresponding tab from the TabPane if it's open.
     * @param deletedNote The Note that was deleted. Can be null.
     */
    @Override
    public void onNoteDeleted(Note deletedNote) {
        if (deletedNote != null && openTabsMap.containsKey(deletedNote.getId())) {
            tabPane.getTabs().remove(openTabsMap.remove(deletedNote.getId()));
        }
    }

    /**
     * Handles the deletion of an entire note set.
     * Iterates through the notes belonging to the deleted set and closes their tabs.
     * @param setId The ID of the NoteSet that was deleted.
     * @param deletedNotes A list of Notes that belonged to the deleted set.
     */
    @Override
    public void onSetDeleted(String setId, List<Note> deletedNotes) {
        for (Note note : deletedNotes) {
            onNoteDeleted(note);
        }
    }

    /**
     * Handles the renaming of a note.
     * Updates the title of the corresponding tab if it's open.
     * Also updates the search index for the renamed note.
     * @param renamedNote The Note that was renamed. Can be null.
     */
    @Override
    public void onNoteRenamed(Note renamedNote) {
        if (renamedNote == null) return;

        Tab tabToUpdate = openTabsMap.get(renamedNote.getId());
        if (tabToUpdate != null) {
            tabToUpdate.setText(renamedNote.getTitle());
        }
        EurekaApp.getSearchService().addOrUpdateNote(renamedNote);
    }

    /**
     * Updates the highlighting in the Sidebar based on the currently selected tab.
     * Finds the Note corresponding to the selected tab's ID and tells the Sidebar
     * to highlight it. If no tab is selected, tells the Sidebar to clear highlighting.
     */
    private void updateSidebarHighlighting() {
        if (sidebar != null) {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                Optional<Note> activeNoteOpt = AppState.getInstance().getNoteById(selectedTab.getId());
                sidebar.updateNoteHighlighting(activeNoteOpt.orElse(null));
            } else {
                sidebar.updateNoteHighlighting(null);
            }
        }
    }
}}