package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.SearchService;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import java.util.Optional;

public class TopBar extends BorderPane {

    private final TextField searchField;
    private final NoteSelectionListener noteSelectionListener;

    private final ListView<SearchService.SearchResult> searchResultsList;
    private final ContextMenu searchResultsPopup;

    public TopBar(NoteSelectionListener listener) {
        this.noteSelectionListener = listener;
        this.setPadding(new Insets(8, 12, 8, 12));
        this.getStyleClass().add("top-bar");

        searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Label title = new Label("Eureka");
        title.getStyleClass().add("app-title");
        HBox titleBox = new HBox(title);
        titleBox.setPrefWidth(200);

        HBox centerBox = new HBox(searchField);
        centerBox.getStyleClass().add("top-bar-center-box");

        this.setLeft(titleBox);
        this.setCenter(centerBox);

        // --- New approach using ContextMenu ---
        searchResultsList = new ListView<>();
        searchResultsList.getStyleClass().add("search-results-list");
        // Hide the "unfocused" border
        searchResultsList.setStyle("-fx-border-color: transparent;");

        CustomMenuItem menuItem = new CustomMenuItem(searchResultsList, false);
        searchResultsPopup = new ContextMenu(menuItem);
        searchResultsPopup.setAutoHide(true);

        setupSearchFunctionality();
    }

    private void setupSearchFunctionality() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                searchResultsPopup.hide();
            } else {
                performSearch(newVal.trim());
            }
        });

        // Hide popup if the search field loses focus
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
        // Calculate height based on number of items, with a max height
        int itemCount = searchResultsList.getItems().size();
        double itemHeight = 50; // Approximate height of a cell
        double newHeight = Math.min(itemCount * itemHeight, 400); // Max height 400
        searchResultsList.setPrefHeight(newHeight);

        searchResultsPopup.show(searchField, Side.BOTTOM, 0, 5);
    }
}