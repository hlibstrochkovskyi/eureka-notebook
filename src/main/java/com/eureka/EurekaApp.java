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

    @Override
    // Replace the entire start() method in EurekaApp.java with this corrected version.
    public void start(Stage primaryStage) {
        // 1. Load data first
        AppState.loadInstance(DataStorageService.loadData());

        // 2. Create the main layout container
        BorderPane rootLayout = new BorderPane();

        // 3. Create the UI components
        EditorContainer editorContainer = new EditorContainer();
        Sidebar sidebar = new Sidebar(editorContainer);

        // TODO: We will create and add the TopBar later
        // rootLayout.setTop(new TopBar());

        // 4. Assemble the components into the SplitPane
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(sidebar, editorContainer);
        splitPane.setDividerPositions(0.30); // Sidebar will take 30% of the width

        // 5. Place the SplitPane in the center of our main layout
        rootLayout.setCenter(splitPane);

        // 6. NOW, create the Scene with the fully prepared rootLayout
        Scene scene = new Scene(rootLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // 7. Set up the main window (Stage) and show it
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