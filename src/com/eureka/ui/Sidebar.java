package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.NoteSet;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Sidebar extends JPanel {
    private final JPanel setsPanel;
    private final AppState appState;
    private final List<SetRow> setRows;
    private final NoteSelectionListener noteSelectionListener;

    public Sidebar(NoteSelectionListener listener) {
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();
        this.setRows = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(new Color(0xF4F4F5));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xE5E7EB)));

        JButton newSetButton = new JButton("New Set");
        newSetButton.setBackground(new Color(0xE11D48));
        newSetButton.setForeground(Color.WHITE);
        newSetButton.setFocusPainted(false);
        newSetButton.setFont(new Font("Arial", Font.BOLD, 14));
        newSetButton.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        buttonPanel.setOpaque(false);
        buttonPanel.add(newSetButton, BorderLayout.CENTER);

        add(buttonPanel, BorderLayout.NORTH);

        setsPanel = new JPanel();
        setsPanel.setLayout(new BoxLayout(setsPanel, BoxLayout.Y_AXIS));
        setsPanel.setBackground(getBackground());
        setsPanel.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(setsPanel);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);

        newSetButton.addActionListener(e -> createNewSet());

        updateSetsList();
    }

    private void createNewSet() {
        String setName = JOptionPane.showInputDialog(this, "Enter Set Name:", "Create New Set", JOptionPane.PLAIN_MESSAGE);
        if (setName != null && !setName.trim().isEmpty()) {
            NoteSet newSet = new NoteSet(setName.trim());
            appState.addSet(newSet);
            updateSetsList();
        }
    }

    public void updateSetsList() {
        setsPanel.removeAll();
        setRows.clear();
        for (NoteSet set : appState.getSets()) {
            SetRow setRow = new SetRow(set, noteSelectionListener, this::updateSetsList);
            setRows.add(setRow);

            JPanel setRowContainer = new JPanel(new BorderLayout());
            setRowContainer.setOpaque(false);
            setRowContainer.add(setRow, BorderLayout.NORTH);
            setRowContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, setRow.getPreferredSize().height));
            setsPanel.add(setRowContainer);
            setsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        setsPanel.revalidate();
        setsPanel.repaint();
    }
}

