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
    public void start(Stage primaryStage) {
        AppState.loadInstance(DataStorageService.loadData());

        primaryStage.setTitle("Eureka");

        BorderPane rootLayout = new BorderPane();

        // Создаем EditorContainer и Sidebar
        EditorContainer editorContainer = new EditorContainer(); // Наш Swing-класс реализует нужный интерфейс
        Sidebar sidebar = new Sidebar(editorContainer);

        // TODO: Позже мы создадим JavaFX TopBar
        // rootLayout.setTop(new TopBar());

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(sidebar, editorContainer);
        splitPane.setDividerPositions(0.30); // Боковая панель займет 30% ширины

        rootLayout.setCenter(splitPane);

        Scene scene = new Scene(rootLayout, 1200, 800);
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