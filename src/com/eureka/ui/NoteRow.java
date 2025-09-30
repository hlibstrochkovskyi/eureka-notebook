// FIX #1: The package must be 'com.eureka.ui'
package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.Note;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NoteRow extends JPanel {
    private final Note note;
    private final NoteSelectionListener noteSelectionListener;
    private final AppState appState;
    private final JLabel titleLabel;

    // FIX #2: The constructor now accepts a 'Runnable' callback.
    public NoteRow(Note note, NoteSelectionListener listener, Runnable onNoteDeletedCallback) {
        this.note = note;
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));

        titleLabel = new JLabel(note.getTitle());

        JButton deleteButton = new JButton("x");
        deleteButton.setMargin(new Insets(0, 2, 0, 2));

        add(titleLabel, BorderLayout.CENTER);
        add(deleteButton, BorderLayout.EAST);

        // Add a click listener to the whole panel to select the note
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                noteSelectionListener.onNoteSelected(note);
            }
        });

        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete the note '" + note.getTitle() + "'?",
                    "Delete Note",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                appState.deleteNote(note.getId());
                noteSelectionListener.onNoteDeleted(note);
                onNoteDeletedCallback.run(); // Refresh the list in the parent SetRow
            }
        });
    }
}
