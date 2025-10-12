package com.eureka;

import com.eureka.model.AppState;
import com.eureka.ui.EditorContainer;
import com.eureka.ui.Sidebar;
import com.eureka.ui.ThemeManager;
import com.eureka.ui.TopBar;
// Remove or comment out this line in EurekaApp.java
// import com.eureka.ui.ThemeManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Paths;


public class EurekaApp extends Application {

    private static SearchService searchService;

    @Override
    public void start(Stage primaryStage) {
        AppState.loadInstance(DataStorageService.loadData());

        // Initialize SearchService
        try {
            searchService = new SearchService(Paths.get(System.getProperty("user.home"), ".eureka"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // --- Layout Setup ---
        // Main content layout
        BorderPane mainContentPane = new BorderPane();

        // Components
        EditorContainer editorContainer = new EditorContainer();
        Sidebar sidebar = new Sidebar(editorContainer);
        editorContainer.setSidebar(sidebar);

        // The TopBar now needs a reference to the NoteSelectionListener to handle clicks
        TopBar topBar = new TopBar(editorContainer);

        mainContentPane.setTop(topBar);
        SplitPane splitPane = new SplitPane(sidebar, editorContainer);
        splitPane.setDividerPositions(0.30);
        mainContentPane.setCenter(splitPane);

        // The root is a StackPane to allow search results to overlay the content
        StackPane rootLayout = new StackPane();
        rootLayout.getChildren().add(mainContentPane);

        // The TopBar will add the search results list to this root pane
        topBar.setRootPane(rootLayout);

        // --- Scene and Stage ---
        Scene scene = new Scene(rootLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        ThemeManager.initialize(scene);

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