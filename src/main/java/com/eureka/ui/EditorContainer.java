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

public class EditorContainer extends BorderPane implements NoteSelectionListener {

    private final TabPane tabPane;
    private final Map<String, Tab> openTabsMap;
    private Sidebar sidebar; // Reference to the sidebar

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

    public void setSidebar(Sidebar sidebar) {
        this.sidebar = sidebar;
    }

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

    private void createNewTab(Note note) {
        Tab tab = new Tab(note.getTitle());
        tab.setId(note.getId());
        TextArea editorArea = new TextArea(note.getContent());
        editorArea.setWrapText(true);
        editorArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");
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

    @Override
    public void onNoteDeleted(Note deletedNote) {
        if (deletedNote != null && openTabsMap.containsKey(deletedNote.getId())) {
            tabPane.getTabs().remove(openTabsMap.remove(deletedNote.getId()));
        }
    }

    @Override
    public void onSetDeleted(String setId, List<Note> deletedNotes) {
        for (Note note : deletedNotes) {
            onNoteDeleted(note);
        }
    }

    @Override
    public void onNoteRenamed(Note renamedNote) {
        if (renamedNote == null) return;
        Tab tabToUpdate = openTabsMap.get(renamedNote.getId());
        if (tabToUpdate != null) {
            tabToUpdate.setText(renamedNote.getTitle());
        }
        EurekaApp.getSearchService().addOrUpdateNote(renamedNote);
    }

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
}