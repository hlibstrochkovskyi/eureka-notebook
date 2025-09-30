// FIX #1: The package must be 'com.eureka.ui' because it's in that folder.
package com.eureka.ui;

// FIX #2: Import classes from their correct packages.
import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// The class now correctly implements the listener from the 'com.eureka' package.
public class EditorContainer extends JPanel implements NoteSelectionListener {

    private final JTextArea editorArea;
    private final JLabel emptyLabel;
    private final JPanel tabsBar;
    private final List<Tab> openTabs;
    private Note currentNote;
    private final AppState appState;
    private boolean isProgrammaticallyChangingText = false;

    public EditorContainer() {
        this.appState = AppState.getInstance();
        this.openTabs = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tabsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabsBar.setBorder(BorderFactory.createEmptyBorder(8, 6, 0, 8));
        tabsBar.setBackground(new Color(0xF4F4F5));

        editorArea = new JTextArea();
        editorArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        editorArea.setLineWrap(true);
        editorArea.setWrapStyleWord(true);
        editorArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        emptyLabel = new JLabel("Select a note to start editing or create a new one.", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        emptyLabel.setForeground(Color.GRAY);

        JScrollPane editorScrollPane = new JScrollPane(editorArea);
        editorScrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        add(tabsBar, BorderLayout.NORTH);
        add(emptyLabel, BorderLayout.CENTER);

        editorArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { saveContent(); }
            public void removeUpdate(DocumentEvent e) { saveContent(); }
            public void changedUpdate(DocumentEvent e) { saveContent(); }
        });
    }

    @Override
    public void onNoteSelected(Note note) {
        if (note == null) return;

        Tab existingTab = findTabForNote(note);
        if (existingTab == null) {
            openNewTab(note);
        } else {
            setActiveNote(note);
        }
    }

    private void openNewTab(Note note) {
        Tab newTab = new Tab(note, e -> closeTab(note), new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveNote(note);
            }
        });
        openTabs.add(newTab);
        tabsBar.add(newTab);
        setActiveNote(note);
        tabsBar.revalidate();
        tabsBar.repaint();
    }

    private void closeTab(Note noteToClose) {
        Tab tabToClose = findTabForNote(noteToClose);
        if (tabToClose == null) return;

        int tabIndex = openTabs.indexOf(tabToClose);
        openTabs.remove(tabToClose);
        tabsBar.remove(tabToClose);

        if (currentNote != null && currentNote.getId().equals(noteToClose.getId())) {
            if (!openTabs.isEmpty()) {
                int newIndex = Math.max(0, tabIndex - 1);
                setActiveNote(openTabs.get(newIndex).getNote());
            } else {
                showEmptyView();
            }
        }

        tabsBar.revalidate();
        tabsBar.repaint();
    }

    private void setActiveNote(Note note) {
        this.currentNote = note;

        if (emptyLabel.isShowing()) {
            remove(emptyLabel);
            add(new JScrollPane(editorArea), BorderLayout.CENTER);
        }

        isProgrammaticallyChangingText = true;
        editorArea.setText(note.getContent());
        editorArea.setEditable(true);
        editorArea.setCaretPosition(0);
        editorArea.requestFocusInWindow();
        isProgrammaticallyChangingText = false;

        updateActiveTabUI();
        revalidate();
        repaint();
    }

    @Override
    public void onNoteDeleted(Note deletedNote) {
        if (deletedNote == null) return;
        closeTab(deletedNote);
    }

    @Override
    public void onSetDeleted(String setId) {
        List<Tab> tabsToRemove = new ArrayList<>();
        for (Tab tab : openTabs) {
            if (tab.getNote().getSetId().equals(setId)) {
                tabsToRemove.add(tab);
            }
        }
        for (Tab tab : tabsToRemove) {
            closeTab(tab.getNote());
        }
    }

    private void showEmptyView() {
        currentNote = null;
        remove(1);
        add(emptyLabel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void saveContent() {
        if (currentNote != null && !isProgrammaticallyChangingText) {
            currentNote.setContent(editorArea.getText());
            currentNote.setUpdatedAt(System.currentTimeMillis());
        }
    }

    private Tab findTabForNote(Note note) {
        for (Tab tab : openTabs) {
            if (tab.getNote().getId().equals(note.getId())) {
                return tab;
            }
        }
        return null;
    }

    private void updateActiveTabUI() {
        for (Tab tab : openTabs) {
            tab.setActive(currentNote != null && tab.getNote().getId().equals(currentNote.getId()));
        }
    }
}
