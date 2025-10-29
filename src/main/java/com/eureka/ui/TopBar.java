package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.I18n;
import com.eureka.NoteSelectionListener;
import com.eureka.SearchService;
import com.eureka.model.AppState;
import com.eureka.model.Note;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the top bar of the application, containing the main menu (Settings, Language, Theme)
 * and the search area (Title, Search Field). It manages the search functionality,
 * including displaying a popup with search results.
 */
public class TopBar extends VBox {

    /**
     * The text field used for entering search queries.
     */
    private final TextField searchField;
    /**
     * Listener to notify when a note is selected from the search results.
     */
    private final NoteSelectionListener noteSelectionListener;
    /**
     * ListView used inside the popup to display search results.
     */
    private final ListView<SearchService.SearchResult> searchResultsList;
    /**
     * ContextMenu acting as a popup to show the searchResultsList below the search field.
     */
    private final ContextMenu searchResultsPopup;

    /**
     * Constructs the TopBar component.
     * Initializes the menu bar, search area, search field, and search results popup.
     * Sets up the layout and wires up the search functionality.
     * @param listener The listener to be notified when a note is selected from search results.
     */
    public TopBar(NoteSelectionListener listener) {
        this.noteSelectionListener = listener;

        MenuBar menuBar = createMenuBar();

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

        searchResultsList = new ListView<>();
        searchResultsList.getStyleClass().add("search-results-list");
        searchResultsList.setStyle("-fx-border-color: transparent;");

        CustomMenuItem menuItem = new CustomMenuItem(searchResultsList, false);
        searchResultsPopup = new ContextMenu(menuItem);
        searchResultsPopup.getStyleClass().add("search-popup");
        searchResultsPopup.setAutoHide(true);

        setupSearchFunctionality();
    }

    /**
     * Creates and configures the main MenuBar for the application.
     * Includes menus for Settings, Language, and Theme.
     * Uses internationalized strings for menu texts.
     * @return The configured MenuBar.
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // --- Settings Menu ---
        Menu settingsMenu = new Menu();
        settingsMenu.textProperty().bind(I18n.bind("menu.settings"));

        // --- Language Submenu ---
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

        // --- Theme Submenu ---
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

    /**
     * Sets up the event listeners and cell factory for the search field and search results list.
     * Handles text changes, focus changes, key presses (Down, Enter, Escape), and mouse clicks
     * to perform searches, navigate results, open notes, and hide the popup.
     */
    private void setupSearchFunctionality() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String trimmedQuery = (newVal != null) ? newVal.trim() : "";
            if (trimmedQuery.isEmpty()) {
                searchResultsPopup.hide();
            } else {
                performSearch(trimmedQuery);
            }
        });

        searchField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && searchResultsPopup.isShowing()) {
            }
        });


        searchField.setOnKeyPressed((KeyEvent event) -> {
            if (searchResultsPopup.isShowing()) {
                if (event.getCode() == KeyCode.DOWN) {
                    searchResultsList.requestFocus();
                    searchResultsList.getSelectionModel().select(0);
                    event.consume();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    searchResultsPopup.hide();
                    event.consume();
                }
            }
        });

        searchResultsList.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                SearchService.SearchResult selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openNoteFromResult(selected);
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                searchResultsPopup.hide();
                searchField.requestFocus();
                event.consume();
            }
        });

        searchResultsList.setOnMouseClicked((MouseEvent event) -> {
            SearchService.SearchResult selected = searchResultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openNoteFromResult(selected);
            }
        });

        searchResultsList.setCellFactory(lv -> new ListCell<SearchService.SearchResult>() {
            private final BorderPane pane = new BorderPane();
            private final Label mainLabel = new Label();
            private final Label wordIndexLabel = new Label();

            {
                mainLabel.getStyleClass().add("result-title");
                wordIndexLabel.getStyleClass().add("result-word-index");
                pane.setLeft(mainLabel);
                pane.setRight(wordIndexLabel);
            }

            /**
             * Called by JavaFX to update the content of the cell.
             * Sets the text of the labels based on the SearchResult item.
             * @param item  The SearchResult item for this cell, or null if the cell is empty.
             * @param empty True if the cell is empty, false otherwise.
             */
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

    /**
     * Performs the search asynchronously in a background thread.
     * Updates the search results popup on the JavaFX Application Thread.
     * @param query The search query string.
     */
    private void performSearch(String query) {
        new Thread(() -> {
            try {
                List<SearchService.SearchResult> results = EurekaApp.getSearchService().search(query);
                Platform.runLater(() -> {
                    if (results.isEmpty()) {
                        searchResultsPopup.hide();
                    } else {
                        searchResultsList.setItems(FXCollections.observableArrayList(results));
                        showSearchResults();
                    }
                });
            } catch (Exception e) {

                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Handles opening a note selected from the search results.
     * Finds the corresponding Note object and notifies the NoteSelectionListener.
     * Hides the search popup and clears the search field.
     * @param result The SearchResult object selected by the user.
     */
    private void openNoteFromResult(SearchService.SearchResult result) {
        Optional<Note> noteOpt = AppState.getInstance().getNoteById(result.noteId());
        noteOpt.ifPresent(note -> {
            noteSelectionListener.onNoteSelectedFromSearch(note, result.position(), result.wordIndex(), result.query());
        });
        searchResultsPopup.hide();
        searchField.clear();
    }

    /**
     * Shows the search results popup below the search field.
     * Adjusts the width and height of the popup list based on the search field width
     * and the number of results, up to a maximum height.
     */
    private void showSearchResults() {
        searchResultsList.setPrefWidth(searchField.getWidth());

        int itemCount = searchResultsList.getItems().size();
        double itemHeight = 30;
        double maxHeight = 400;
        double newHeight = Math.min(itemCount * itemHeight, maxHeight);
        searchResultsList.setPrefHeight(newHeight);

        if (!searchResultsPopup.isShowing()) {
            searchResultsPopup.show(searchField, Side.BOTTOM, 0, 5);
        }
    }
}