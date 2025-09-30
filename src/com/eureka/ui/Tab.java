package com.eureka.ui;

import com.eureka.model.Note;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Tab extends JPanel {
    private final Note note;
    private final JLabel titleLabel;
    private boolean isActive = false;

    public Tab(Note note, ActionListener closeListener, MouseAdapter selectListener) {
        this.note = note;
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        setOpaque(false);
        setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#3b82f6")));


        titleLabel = new JLabel(note.getTitle());

        JButton closeButton = new JButton("x");
        closeButton.setMargin(new Insets(0, 4, 0, 4));
        closeButton.addActionListener(closeListener);

        add(titleLabel);
        add(closeButton);

        addMouseListener(selectListener);
    }

    public Note getNote() {
        return note;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (isActive) {
            setBackground(Color.WHITE);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#3b82f6")));
        } else {
            setBackground(new Color(0xF3F4F6));
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN));
            setBorder(null);
        }
        setOpaque(true);
    }
}