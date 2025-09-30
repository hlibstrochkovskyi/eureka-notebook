package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.model.NoteSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SetRow extends JPanel {
    private final NoteSet noteSet;
    private final JPanel notesPanel;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private boolean isExpanded = false;

    public SetRow(NoteSet noteSet, NoteSelectionListener listener, Runnable onSetDeletedCallback) {
        this.noteSet = noteSet;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0xE5E7EB));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel setNameLabel = new JLabel(noteSet.getName());
        setNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        setNameLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        buttonsPanel.setOpaque(false);

        JButton addButton = new JButton("+");
        JButton deleteButton = new JButton("x");

        buttonsPanel.add(addButton);
        buttonsPanel.add(deleteButton);

        headerPanel.add(setNameLabel, BorderLayout.CENTER);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);

        notesPanel = new JPanel();
        notesPanel.setLayout(new BoxLayout(notesPanel, BoxLayout.Y_AXIS));
        notesPanel.setOpaque(false);
        notesPanel.setBorder(BorderFactory.createEmptyBorder(4, 15, 4, 4));
        notesPanel.setVisible(false);

        add(headerPanel);
        add(notesPanel);

        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleExpand();
            }
        });

        addButton.addActionListener(e -> addNewNote());

        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete the set '" + noteSet.getName() + "' and all its notes?",
                    "Delete Set",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                appState.deleteSet(noteSet.getId());
                noteSelectionListener.onSetDeleted(noteSet.getId());
                onSetDeletedCallback.run();
            }
        });
    }

    private void toggleExpand() {
        isExpanded = !isExpanded;
        notesPanel.setVisible(isExpanded);
        if (isExpanded) {
            refreshNotesList();
        }
        revalidate();
        repaint();
    }

    private void addNewNote() {
        if (!isExpanded) {
            toggleExpand();
        }
        String noteTitle = JOptionPane.showInputDialog(this, "Enter Note Title:", "Create New Note", JOptionPane.PLAIN_MESSAGE);
        if (noteTitle != null && !noteTitle.trim().isEmpty()) {
            Note newNote = new Note(noteSet.getId(), noteTitle.trim());
            appState.addNote(newNote);
            refreshNotesList();
            noteSelectionListener.onNoteSelected(newNote);
        }
    }

    private void refreshNotesList() {
        notesPanel.removeAll();
        List<Note> notesInSet = appState.getNotesForSet(noteSet.getId());
        for (Note note : notesInSet) {
            NoteRow noteRow = new NoteRow(note, noteSelectionListener, this::refreshNotesList);
            notesPanel.add(noteRow);
            notesPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        notesPanel.revalidate();
        notesPanel.repaint();
    }
}
