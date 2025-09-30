package com.eureka.ui;

import javax.swing.*;
import java.awt.*;

public class TopBar extends JPanel {

    public TopBar() {
        // Set the layout for the top bar
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 56)); // Height of 56
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // Create left, center, and right panels for alignment
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        JPanel centerPanel = new JPanel();
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));

        // Make panels transparent to inherit the top bar's background
        leftPanel.setOpaque(false);
        centerPanel.setOpaque(false);
        rightPanel.setOpaque(false);

        // --- Left Panel ---
        JLabel appTitle = new JLabel("Eureka");
        appTitle.setFont(new Font("Arial", Font.BOLD, 18));
        appTitle.setForeground(new Color(0xE11D48)); // A shade of red
        leftPanel.add(appTitle);
        // Add a vertical strut to center the title vertically
        leftPanel.add(Box.createVerticalStrut(56));

        // --- Center Panel ---
        JTextField searchInput = new JTextField(30);
        searchInput.setPreferredSize(new Dimension(400, 36));
        // Add some basic styling later
        centerPanel.add(searchInput);
        centerPanel.add(Box.createVerticalStrut(56));


        // --- Right Panel ---
        JButton settingsButton = new JButton("Settings");
        JButton helpButton = new JButton("Help");
        styleTopBarButton(settingsButton);
        styleTopBarButton(helpButton);
        rightPanel.add(settingsButton);
        rightPanel.add(helpButton);
        rightPanel.add(Box.createVerticalStrut(56));

        // Add the panels to the top bar
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void styleTopBarButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
    }
}
