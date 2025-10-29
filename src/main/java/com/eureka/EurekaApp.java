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
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The main entry point for the Eureka Note-Taking Application.
 * Extends JavaFX Application and sets up the primary stage, layout,
 * loads initial data, initializes services, and handles application lifecycle events.
 */
public class EurekaApp extends Application {

    /**
     * Static reference to the SearchService instance used throughout the application.
     */
    private static SearchService searchService;

    /**
     * The main entry point for all JavaFX applications.
     * This method is called after the JFX toolkit is initialized.
     * It sets up the primary stage (window), loads application state,
     * initializes the UI components (Sidebar, EditorContainer, TopBar),
     * sets up the scene, applies the theme, and shows the window.
     * It also initializes the SearchService.
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        AppState.loadInstance(DataStorageService.loadData());

        try {
            Path searchIndexPath = Paths.get(System.getProperty("user.home"), ".eureka");
            searchService = new SearchService(searchIndexPath);
        } catch (IOException e) {
            System.err.println("Failed to initialize SearchService:");
            e.printStackTrace();
            Platform.exit();
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

        ThemeManager.initialize(scene);

        primaryStage.setTitle("Eureka");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (AppState.getInstance().isSidebarCollapsed()) {
            sidebar.collapse(false);
        }
    }

    /**
     * This method is called when the application should stop, and provides a
     * convenient place to handle application cleanup or saving state.
     * Saves the current application state before exiting.
     */
    @Override
    public void stop() {
        DataStorageService.saveData(AppState.getInstance());
        System.out.println("Application stopped and data saved.");
    }

    /**
     * Provides static access to the single SearchService instance.
     * @return The application's SearchService instance.
     */
    public static SearchService getSearchService() {
        return searchService;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}