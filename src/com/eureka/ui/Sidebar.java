package com.eureka.ui;

import com.eureka.NoteSelectionListener;
import com.eureka.model.AppState;
import com.eureka.model.NoteSet;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.TextInputDialog;

import java.util.Optional;

public class Sidebar extends BorderPane {

    private final VBox setsPanel; // pannel to show sets panel
    private final AppState appState;
    private final NoteSelectionListener noteSelectionListener;

    public Sidebar(NoteSelectionListener listener) {
        this.noteSelectionListener = listener;
        this.appState = AppState.getInstance();

        // Стилизация и настройка основной панели
        this.setPadding(new Insets(12));
        this.setStyle("-fx-background-color: #f4f4f5; -fx-border-width: 0 1 0 0; -fx-border-color: #e5e7eb;");
        this.setPrefWidth(280);

        // 1. Кнопка "New Set"
        Button newSetButton = new Button("New Set");
        newSetButton.setMaxWidth(Double.MAX_VALUE); // Растянуть на всю ширину
        newSetButton.setStyle("-fx-background-color: #e11d48; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        newSetButton.setOnAction(e -> createNewSet()); // Обработчик нажатия

        this.setTop(newSetButton); // Размещаем кнопку вверху

        // 2. Панель для списков заметок с прокруткой
        setsPanel = new VBox(8); // VBox с отступом 8px между элементами
        setsPanel.setPadding(new Insets(12, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane(setsPanel);
        scrollPane.setFitToWidth(true); // Растягивать контент по ширине
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Отключить горизонтальную прокрутку

        this.setCenter(scrollPane); // Размещаем панель с прокруткой в центре

        // Первоначальное отображение всех сетов
        updateSetsList();
    }

    /**
     * Открывает диалоговое окно для создания нового сета.
     */
    private void createNewSet() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Set");
        dialog.setHeaderText("Enter the name for the new set:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                NoteSet newSet = new NoteSet(name.trim());
                appState.addSet(newSet);
                updateSetsList(); // Обновляем UI
            }
        });
    }

    /**
     * Обновляет список сетов на боковой панели.
     */
    public void updateSetsList() {
        setsPanel.getChildren().clear(); // Очищаем старый список
        for (NoteSet set : appState.getSets()) {
            // TODO: Мы создадим JavaFX-версию SetRow на следующем шаге
            // SetRow setRow = new SetRow(set, noteSelectionListener, this::updateSetsList);
            // setsPanel.getChildren().add(setRow);

            // Временная заглушка, пока у нас нет SetRow
            Label placeholder = new Label("▶ " + set.getName());
            placeholder.setStyle("-fx-font-size: 14px; -fx-padding: 8px; -fx-background-color: #e5e7eb; -fx-background-radius: 4;");
            placeholder.setMaxWidth(Double.MAX_VALUE);
            setsPanel.getChildren().add(placeholder);
        }
    }
}
