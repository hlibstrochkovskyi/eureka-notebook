package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A UI component that displays a single note set in the sidebar.
 */
public class SetRow extends JPanel {

    private final NoteSet noteSet;
    private final JPanel notesPanel;
    private boolean isExpanded = false;
    private final AppState appState;
    private final NoteSelectionListener noteSelectionListener;

    public SetRow(NoteSet noteSet, NoteSelectionListener listener) {
        this.noteSet = noteSet;
        this.appState = AppState.getInstance();
        this.noteSelectionListener = listener;

        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Main row for the set name and buttons
        JPanel mainRow = new JPanel(new BorderLayout(10, 0));
        mainRow.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        mainRow.setOpaque(false);

        JLabel nameLabel = new JLabel(noteSet.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setIcon(UIManager.getIcon("Tree.closedIcon"));

        // Panel for action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionsPanel.setOpaque(false);
        JButton addButton = new JButton("+");
        JButton moreButton = new JButton("...");

        styleActionButton(addButton);
        styleActionButton(moreButton);

        actionsPanel.add(addButton);
        actionsPanel.add(moreButton);

        mainRow.add(nameLabel, BorderLayout.CENTER);
        mainRow.add(actionsPanel, BorderLayout.EAST);

        // Panel to hold notes for this set, initially hidden
        notesPanel = new JPanel();
        notesPanel.setLayout(new BoxLayout(notesPanel, BoxLayout.Y_AXIS));
        notesPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
        notesPanel.setOpaque(false);
        notesPanel.setVisible(false);

        // This panel holds both the main row and the notes panel
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.add(mainRow);
        container.add(notesPanel);

        add(container, BorderLayout.CENTER);

        // Add listeners
        mainRow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleExpansion();
            }
        });

        addButton.addActionListener(e -> createNewNote());

        updateNotesList();
    }

    private void toggleExpansion() {
        isExpanded = !isExpanded;
        notesPanel.setVisible(isExpanded);
        // Update icon - pseudo-code
        JLabel label = (JLabel) ((JPanel) getComponent(0)).getComponent(0);
        label.setIcon(isExpanded ? UIManager.getIcon("Tree.openIcon") : UIManager.getIcon("Tree.closedIcon"));

        // We need to revalidate the parent to reflect size changes
        getParent().revalidate();
        getParent().repaint();
    }

    private void createNewNote() {
        String noteTitle = JOptionPane.showInputDialog(this, "Enter com.eureka.model.Note Title:", "Create New com.eureka.model.Note", JOptionPane.PLAIN_MESSAGE);
        if (noteTitle != null && !noteTitle.trim().isEmpty()) {
            Note newNote = new Note(noteSet.getId(), noteTitle.trim());
            appState.addNote(newNote);
            updateNotesList();
            if (!isExpanded) {
                toggleExpansion();
            }
        }
    }

    private void updateNotesList() {
        notesPanel.removeAll();
        java.util.List<Note> notes = appState.getNotesForSet(noteSet.getId());
        if (notes.isEmpty()) {
            JLabel emptyLabel = new JLabel("No notes in this set.");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            emptyLabel.setForeground(Color.GRAY);
            notesPanel.add(emptyLabel);
        } else {
            for (Note note : notes) {
                NoteRow noteRow = new NoteRow(note);
                noteRow.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (noteSelectionListener != null) {
                            noteSelectionListener.onNoteSelected(note);
                        }
                    }
                });
                notesPanel.add(noteRow);
            }
        }
        notesPanel.revalidate();
        notesPanel.repaint();
    }

    private void styleActionButton(JButton button) {
        button.setMargin(new Insets(2, 5, 2, 5));
        button.setFocusPainted(false);
    }
}

