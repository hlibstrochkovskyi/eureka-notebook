package com.eureka.ui;

import com.eureka.model.Note;
import javax.swing.*;
import java.awt.*;

/**
 * A UI component that displays a single note chip in the sidebar.
 */
public class NoteRow extends JPanel {
    private final Note note;

    public NoteRow(Note note) {
        this.note = note;
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setBackground(new Color(0xE5E7EB)); // A light gray for the chip

        JLabel titleLabel = new JLabel(note.getTitle());
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        add(titleLabel, BorderLayout.CENTER);
    }

    public Note getNote() {
        return note;
    }
}
