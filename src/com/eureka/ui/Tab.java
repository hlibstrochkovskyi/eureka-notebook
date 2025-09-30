package com.eureka.ui;

import com.eureka.model.Note;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

/**
 * A UI component representing a single tab for an open note.
 */
public class Tab extends JPanel {
    private final Note note;
    private final JLabel titleLabel;

    public Tab(Note note, ActionListener closeListener, MouseAdapter selectListener) {
        this.note = note;
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        titleLabel = new JLabel(note.getTitle());
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JButton closeButton = new JButton("x");
        closeButton.setMargin(new Insets(0, 2, 0, 2));
        closeButton.setFont(new Font("Arial", Font.BOLD, 12));
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(closeListener);

        add(titleLabel);
        add(closeButton);

        // Add the mouse listener to the entire tab for selection
        addMouseListener(selectListener);
        // Also add to the label to ensure clicks on the text are captured
        titleLabel.addMouseListener(selectListener);
    }

    public Note getNote() {
        return note;
    }

    public void setActive(boolean isActive) {
        if (isActive) {
            setBackground(Color.WHITE);
            titleLabel.setForeground(new Color(0x3B82F6)); // Active blue color
            titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        } else {
            setBackground(new Color(0xF4F4F5)); // Default background
            titleLabel.setForeground(Color.BLACK);
            titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        }
        setOpaque(isActive); // Only the active tab has a solid background
    }
}
