package com.eureka;

import com.eureka.model.AppState;
import com.eureka.ui.EditorContainer; // Импортируем заглушку EditorContainer
import com.eureka.ui.Sidebar;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class EurekaApp extends Application {


    // Replace the entire start() method in EurekaApp.java with this corrected version.
    // Replace the start method with this
    @Override
    public void start(Stage primaryStage) {
        AppState.loadInstance(DataStorageService.loadData());

        // 1. Create the components
        EditorContainer editorContainer = new EditorContainer();
        Sidebar sidebar = new Sidebar(editorContainer); // Pass the editor as a listener

        // 2. Link them back so the editor can talk to the sidebar
        editorContainer.setSidebar(sidebar);

        // 3. Assemble the main layout
        BorderPane rootLayout = new BorderPane();
        SplitPane splitPane = new SplitPane(sidebar, editorContainer);
        splitPane.setDividerPositions(0.30);
        rootLayout.setCenter(splitPane);

        // 4. Create and show the scene
        Scene scene = new Scene(rootLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("Eureka");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DataStorageService.saveData(AppState.getInstance());
        System.out.println("Application is closing, data saved.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}