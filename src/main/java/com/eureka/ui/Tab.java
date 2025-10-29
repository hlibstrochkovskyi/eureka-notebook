package com.eureka.ui;

import com.eureka.model.Note;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Represents a custom UI component for a single tab, likely within a tabbed pane,
 * displaying a note's title and a close button. This class uses Swing components.
 * It manages its active state visually (background, font, border).
 */
public class Tab extends JPanel {
    /**
     * The Note data object associated with this tab.
     */
    private final Note note;
    /**
     * The JLabel displaying the title of the note.
     */
    private final JLabel titleLabel;
    /**
     * Tracks whether this tab is currently the active/selected one.
     */
    private boolean isActive = false;

    /**
     * Constructs a new Tab component.
     * Initializes the layout, border, title label, and close button.
     * Attaches the provided listeners for close and selection actions.
     * @param note           The Note object this tab represents.
     * @param closeListener  The ActionListener to be called when the close button is clicked.
     * @param selectListener The MouseAdapter to handle mouse events on the tab itself (e.g., for selection).
     */
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

    /**
     * Gets the Note object associated with this tab.
     * @return The Note object.
     */
    public Note getNote() {
        return note;
    }

    /**
     * Sets the visual state of the tab to active or inactive.
     * Modifies the background color, title font (bold/plain), and border
     * to visually indicate whether the tab is selected.
     * @param active true to set the tab as active, false for inactive.
     */
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