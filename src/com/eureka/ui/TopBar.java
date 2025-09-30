package com.eureka.ui;

import javax.swing.*;
import java.awt.*;

public class TopBar extends JPanel {

    public TopBar() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(0, 56));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5E7EB)));

        // Left side: App Title
        JLabel appTitle = new JLabel("Eureka");
        appTitle.setFont(new Font("Arial", Font.BOLD, 18));
        appTitle.setForeground(new Color(0xE11D48)); // Accent color
        appTitle.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        // Center: Search Bar (placeholder for now)
        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField(30);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);

        // Right side: Buttons (placeholders for now)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
        buttonsPanel.add(new JButton("Settings"));
        buttonsPanel.add(new JButton("Help"));

        add(appTitle, BorderLayout.WEST);
        add(searchPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.EAST);
    }
}