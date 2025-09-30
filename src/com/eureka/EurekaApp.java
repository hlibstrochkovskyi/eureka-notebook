package com.eureka;

import com.eureka.model.AppState;
import com.eureka.ui.EditorContainer;
import com.eureka.ui.Sidebar;
import com.eureka.ui.TopBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EurekaApp {

    public static void main(String[] args) {
        // Load data from the file FIRST
        AppState.loadInstance(DataStorageService.loadData());

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Eureka");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);

            // Add a listener to save data when the window is closing
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    DataStorageService.saveData(AppState.getInstance());
                }
            });

            frame.setLayout(new BorderLayout());
            frame.add(new TopBar(), BorderLayout.NORTH);

            EditorContainer editorContainer = new EditorContainer();
            Sidebar sidebar = new Sidebar(editorContainer);
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, editorContainer);
            splitPane.setDividerLocation(280);
            frame.add(splitPane, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}