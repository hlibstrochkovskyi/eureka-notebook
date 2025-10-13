package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.I18n;
import com.eureka.NoteSelectionListener;
import com.eureka.SearchService;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import com.eureka.ui.ThemeManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class TopBar extends VBox {

    private final TextField searchField;
    private final NoteSelectionListener noteSelectionListener;
    private final ListView<SearchService.SearchResult> searchResultsList;
    private final ContextMenu searchResultsPopup;

    public TopBar(NoteSelectionListener listener) {
        this.noteSelectionListener = listener;

        // --- Menu Bar ---
        MenuBar menuBar = createMenuBar();

        // --- Search Bar Area ---
        HBox searchArea = new HBox(12);
        searchArea.setPadding(new Insets(4, 12, 8, 12));
        searchArea.getStyleClass().add("search-area");

        Label title = new Label();
        title.textProperty().bind(I18n.bind("app.title"));
        title.getStyleClass().add("app-title");
        title.setMinWidth(180);

        searchField = new TextField();
        searchField.promptTextProperty().bind(I18n.bind("search.prompt"));
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchArea.getChildren().addAll(title, searchField);

        this.getChildren().addAll(menuBar, searchArea);

        // --- Search Popup Initialization ---
        searchResultsList = new ListView<>();
        searchResultsList.getStyleClass().add("search-results-list");
        searchResultsList.setStyle("-fx-border-color: transparent;");
        CustomMenuItem menuItem = new CustomMenuItem(searchResultsList, false);
        searchResultsPopup = new ContextMenu(menuItem);
        searchResultsPopup.getStyleClass().add("search-popup");
        searchResultsPopup.setAutoHide(true);

        // This method was missing in the previous version
        setupSearchFunctionality();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu settingsMenu = new Menu();
        settingsMenu.textProperty().bind(I18n.bind("menu.settings"));

        Menu languageMenu = new Menu();
        languageMenu.textProperty().bind(I18n.bind("menu.language"));

        Map<String, Locale> languages = new LinkedHashMap<>();
        languages.put("中文", Locale.CHINESE);
        languages.put("English", Locale.ENGLISH);
        languages.put("Français", Locale.FRENCH);
        languages.put("Deutsch", Locale.GERMAN);
        languages.put("Español", new Locale("es"));
        languages.put("Українська", new Locale("uk"));

        ToggleGroup langToggleGroup = new ToggleGroup();
        for (Map.Entry<String, Locale> entry : languages.entrySet()) {
            RadioMenuItem langItem = new RadioMenuItem(entry.getKey());
            langItem.setToggleGroup(langToggleGroup);
            langItem.setUserData(entry.getValue());
            if (I18n.getLocale().getLanguage().equals(entry.getValue().getLanguage())) {
                langItem.setSelected(true);
            }
            langItem.setOnAction(e -> I18n.setLocale((Locale) langItem.getUserData()));
            languageMenu.getItems().add(langItem);
        }

        Menu themeMenu = new Menu();
        themeMenu.textProperty().bind(I18n.bind("menu.theme"));
        ToggleGroup themeToggleGroup = new ToggleGroup();

        for (ThemeManager.Theme theme : ThemeManager.Theme.values()) {
            RadioMenuItem themeItem = new RadioMenuItem(theme.name());
            themeItem.setToggleGroup(themeToggleGroup);
            themeItem.setUserData(theme);
            if (ThemeManager.getCurrentTheme() == theme) {
                themeItem.setSelected(true);
            }
            themeItem.setOnAction(e -> ThemeManager.setTheme((ThemeManager.Theme) themeItem.getUserData()));
            themeMenu.getItems().add(themeItem);
        }

        settingsMenu.getItems().addAll(languageMenu, themeMenu);
        menuBar.getMenus().add(settingsMenu);
        return menuBar;
    }

    // --- ALL THE SEARCH METHODS THAT WERE MISSING ---

    private void setupSearchFunctionality() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                searchResultsPopup.hide();
            } else {
                performSearch(newVal.trim());
            }
        });

        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                searchResultsPopup.hide();
            }
        });

        searchField.setOnKeyPressed(event -> {
            if (searchResultsPopup.isShowing()) {
                if (event.getCode() == KeyCode.DOWN) {
                    searchResultsList.requestFocus();
                    searchResultsList.getSelectionModel().select(0);
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    searchResultsPopup.hide();
                }
            }
        });

        searchResultsList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SearchService.SearchResult selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openNoteFromResult(selected);
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                searchResultsPopup.hide();
            }
        });

        searchResultsList.setOnMouseClicked(event -> {
            SearchService.SearchResult selected = searchResultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openNoteFromResult(selected);
            }
        });

        searchResultsList.setCellFactory(lv -> new ListCell<>() {
            private final BorderPane pane = new BorderPane();
            private final Label mainLabel = new Label();
            private final Label wordIndexLabel = new Label();
            {
                mainLabel.getStyleClass().add("result-title");
                wordIndexLabel.getStyleClass().add("result-word-index");
                pane.setLeft(mainLabel);
                pane.setRight(wordIndexLabel);
            }
            @Override
            protected void updateItem(SearchService.SearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    mainLabel.setText(item.setName() + "  ›  " + item.title());
                    wordIndexLabel.setText("Word: " + item.wordIndex());
                    setGraphic(pane);
                }
            }
        });
    }

    private void performSearch(String query) {
        new Thread(() -> {
            try {
                var results = EurekaApp.getSearchService().search(query);
                Platform.runLater(() -> {
                    if (results.isEmpty()) {
                        searchResultsPopup.hide();
                    } else {
                        searchResultsList.setItems(FXCollections.observableArrayList(results));
                        showSearchResults();
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void openNoteFromResult(SearchService.SearchResult result) {
        Optional<Note> noteOpt = AppState.getInstance().getNoteById(result.noteId());
        noteOpt.ifPresent(note -> {
            noteSelectionListener.onNoteSelectedFromSearch(note, result.position(), result.wordIndex(), result.query());
        });
        searchResultsPopup.hide();
        searchField.clear();
    }

    private void showSearchResults() {
        searchResultsList.setPrefWidth(searchField.getWidth());
        int itemCount = searchResultsList.getItems().size();
        double itemHeight = 50;
        double newHeight = Math.min(itemCount * itemHeight, 400);
        searchResultsList.setPrefHeight(newHeight);
        searchResultsPopup.show(searchField, Side.BOTTOM, 0, 5);
    }
}