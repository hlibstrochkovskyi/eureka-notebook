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




    // Replace the start() method in EurekaApp.java
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
        SplitPane splitPane = new SplitPane();
        EditorContainer editorContainer = new EditorContainer();
        Sidebar sidebar = new Sidebar(editorContainer, splitPane);
        editorContainer.setSidebar(sidebar);
        splitPane.getItems().addAll(sidebar, editorContainer);
        splitPane.setDividerPositions(0.30);
        TopBar topBar = new TopBar(editorContainer);
        rootLayout.setTop(topBar);
        rootLayout.setCenter(splitPane);

        Scene scene = new Scene(rootLayout, 1200, 800);

        // ThemeManager now handles all stylesheet loading.
        ThemeManager.initialize(scene);

        primaryStage.setTitle("Eureka");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (AppState.getInstance().isSidebarCollapsed()) {
            sidebar.collapse(false);
        }
    }
    //code
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