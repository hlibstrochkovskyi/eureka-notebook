package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.NoteSet;

import javax.swing.*;
import java.awt.*;

public class Sidebar extends JPanel {

    private final JPanel setsListPanel;
    private final AppState appState;
    private final NoteSelectionListener noteSelectionListener;

    public Sidebar(NoteSelectionListener listener) {
        this.noteSelectionListener = listener;
        // Set the layout for the sidebar
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(280, 0));
        setBackground(new Color(0xF4F4F5)); // Light gray background
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        this.appState = AppState.getInstance();

        // Create a panel for the top part of the sidebar
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        topPanel.setOpaque(false); // Make it transparent to show sidebar background

        // Add the "New Set" button
        JButton newSetButton = new JButton("New Set");
        newSetButton.setBackground(new Color(0xE11D48));
        newSetButton.setForeground(Color.WHITE);
        newSetButton.setFont(new Font("Arial", Font.BOLD, 14));
        newSetButton.setFocusPainted(false);
        newSetButton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        newSetButton.addActionListener(e -> createNewSet());
        topPanel.add(newSetButton);

        add(topPanel, BorderLayout.NORTH);

        // This panel will hold the list of note sets
        setsListPanel = new JPanel();
        setsListPanel.setLayout(new BoxLayout(setsListPanel, BoxLayout.Y_AXIS));
        setsListPanel.setOpaque(false);
        setsListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);


        // Use a scroll pane in case the list of sets gets long
        JScrollPane scrollPane = new JScrollPane(setsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);

        // Initial population of the sets list
        updateSetsList();
    }

    /**
     * Opens a dialog to get a name for a new set and adds it to the state.
     */
    private void createNewSet() {
        String setName = JOptionPane.showInputDialog(this, "Enter Set Name:", "Create New Set", JOptionPane.PLAIN_MESSAGE);
        if (setName != null && !setName.trim().isEmpty()) {
            NoteSet newSet = new NoteSet(setName.trim());
            appState.addSet(newSet);
            updateSetsList();
        }
    }

    /**
     * Clears and redraws the list of sets in the sidebar.
     */
    private void updateSetsList() {
        setsListPanel.removeAll();
        java.util.List<NoteSet> currentSets = appState.getSets();

        if (currentSets.isEmpty()) {
            JLabel emptyLabel = new JLabel("No sets created yet.");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setForeground(Color.GRAY);
            setsListPanel.add(emptyLabel);
        } else {
            for (NoteSet set : currentSets) {
                setsListPanel.add(new SetRow(set, noteSelectionListener));
            }
        }
        // Refresh the panel
        setsListPanel.revalidate();
        setsListPanel.repaint();
    }
}