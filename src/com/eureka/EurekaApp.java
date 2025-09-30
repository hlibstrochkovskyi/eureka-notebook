// FIX #1: The package must match the full folder path.
package com.eureka;

// FIX #2: We need to import all the UI classes we use from the 'ui' package.
import com.eureka.ui.EditorContainer;
import com.eureka.ui.Sidebar;
import com.eureka.ui.TopBar;

import javax.swing.*;
import java.awt.*;

public class EurekaApp {

    public static void main(String[] args) {
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

            frame.setLayout(new BorderLayout());

            // These lines should now work because TopBar and Sidebar are imported correctly.
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

