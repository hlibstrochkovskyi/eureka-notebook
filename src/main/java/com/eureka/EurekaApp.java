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
            return;
        }

        BorderPane rootLayout = new BorderPane();

        // --- THIS IS THE MAIN CHANGE ---
        // 1. Create SplitPane first
        SplitPane splitPane = new SplitPane();

        // 2. Create components and pass SplitPane to Sidebar
        EditorContainer editorContainer = new EditorContainer();
        Sidebar sidebar = new Sidebar(editorContainer, splitPane);
        editorContainer.setSidebar(sidebar);

        // 3. Add components to the SplitPane
        splitPane.getItems().addAll(sidebar, editorContainer);
        splitPane.setDividerPositions(0.30);
        // --- END OF CHANGE ---

        TopBar topBar = new TopBar(editorContainer);
        rootLayout.setTop(topBar);
        rootLayout.setCenter(splitPane);

        Scene scene = new Scene(rootLayout, 1200, 800);

        // Load stylesheets
        ThemeManager.initialize(scene);
        String cssPath = getClass().getResource("/styles.css").toExternalForm();
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        primaryStage.setTitle("Eureka");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Apply the saved collapsed state after the stage is shown
        if (AppState.getInstance().isSidebarCollapsed()) {
            sidebar.collapse(false); // false = no animation on startup
        }
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