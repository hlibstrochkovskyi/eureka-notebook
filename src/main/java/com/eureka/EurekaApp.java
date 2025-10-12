package com.eureka;

import com.eureka.model.AppState;
import com.eureka.ui.EditorContainer;
import com.eureka.ui.Sidebar;
import com.eureka.ui.ThemeManager;
import com.eureka.ui.TopBar;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Paths;

public class EurekaApp extends Application {

    private static SearchService searchService;

    @Override
    public void start(Stage primaryStage) {
        AppState.loadInstance(DataStorageService.loadData());

        try {
            searchService = new SearchService(Paths.get(System.getProperty("user.home"), ".eureka"));
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error gracefully
            return;
        }

        // --- Use BorderPane as the root layout ---
        BorderPane rootLayout = new BorderPane();

        // Components
        EditorContainer editorContainer = new EditorContainer();
        Sidebar sidebar = new Sidebar(editorContainer);
        editorContainer.setSidebar(sidebar);

        // The new TopBar no longer needs a reference to the root pane
        TopBar topBar = new TopBar(editorContainer);

        rootLayout.setTop(topBar);
        SplitPane splitPane = new SplitPane(sidebar, editorContainer);
        splitPane.setDividerPositions(0.30);
        rootLayout.setCenter(splitPane);

        // --- Scene and Stage ---
        Scene scene = new Scene(rootLayout, 1200, 800);

        // Safely get and add the stylesheet
        String cssPath = getClass().getResource("/styles.css").toExternalForm();
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        } else {
            System.err.println("Warning: styles.css not found.");
        }

        // ThemeManager call is ready for when you want to implement it
        // ThemeManager.initialize(scene);

        primaryStage.setTitle("Eureka");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        DataStorageService.saveData(AppState.getInstance());
    }

    public static SearchService getSearchService() {
        return searchService;
    }

    public static void main(String[] args) {
        launch(args);
    }
}