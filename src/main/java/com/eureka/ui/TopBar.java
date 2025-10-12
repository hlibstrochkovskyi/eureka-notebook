package com.eureka.ui;

import com.eureka.EurekaApp;
import com.eureka.NoteSelectionListener;
import com.eureka.SearchService;
import com.eureka.model.AppState;
import com.eureka.model.Note;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.Optional;

/**
 * The TopBar of the application, contains the title and the global search functionality.
 */
public class TopBar extends BorderPane {

    private final TextField searchField;
    private final NoteSelectionListener noteSelectionListener;
    private StackPane rootPane; // To overlay search results

    // UI components for search results overlay
    private final ListView<SearchService.SearchResult> searchResultsList;
    private final Pane backdrop;

    public TopBar(NoteSelectionListener listener) {
        this.noteSelectionListener = listener;
        this.setPadding(new Insets(8, 12, 8, 12));
        this.getStyleClass().add("top-bar");

        // --- Search Field ---
        searchField = new TextField();
        searchField.setPromptText("Search"); // As per FEAT-010
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // --- Layout ---
        Label title = new Label("Eureka");
        title.getStyleClass().add("app-title");
        HBox titleBox = new HBox(title);
        titleBox.setPrefWidth(200); // Give it some space

        HBox centerBox = new HBox(searchField);
        centerBox.getStyleClass().add("top-bar-center-box");

        this.setLeft(titleBox);
        this.setCenter(centerBox);

        // --- Search Results UI Initialization ---
        this.searchResultsList = new ListView<>();
        this.searchResultsList.getStyleClass().add("search-results-list");
        this.searchResultsList.setVisible(false); // Initially hidden

        this.backdrop = new Pane();
        this.backdrop.getStyleClass().add("search-backdrop");
        this.backdrop.setVisible(false); // Initially hidden
        this.backdrop.setOnMouseClicked(e -> hideSearchResults());

        setupSearchFunctionality();
    }

    /**
     * This method must be called from EurekaApp to enable the search results overlay.
     * @param root The root StackPane of the scene.
     */
    public void setRootPane(StackPane root) {
        this.rootPane = root;
        // Ensure overlay components are not managed by layout when invisible
        backdrop.setManaged(false);
        searchResultsList.setManaged(false);
        // Add to root pane, they will be hidden until needed
        rootPane.getChildren().addAll(backdrop, searchResultsList);
    }

    private void setupSearchFunctionality() {
        // --- Listen for text changes to trigger search ---
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                hideSearchResults();
            } else {
                performSearch(newVal.trim());
            }
        });

        // --- Handle keyboard events for navigation ---
        searchField.setOnKeyPressed(event -> {
            if (searchResultsList.isVisible()) {
                if (event.getCode() == KeyCode.DOWN) {
                    searchResultsList.requestFocus();
                    searchResultsList.getSelectionModel().select(0);
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    hideSearchResults();
                } else if (event.getCode() == KeyCode.ENTER) {
                    // Prevent default behavior (like triggering a button)
                    event.consume();
                }
            }
        });

        // Handle ENTER on search field to select first result
        searchField.setOnAction(event -> {
            if (searchResultsList.isVisible() && !searchResultsList.getItems().isEmpty()) {
                searchResultsList.getSelectionModel().selectFirst();
                openNoteFromResult(searchResultsList.getSelectionModel().getSelectedItem());
            }
        });

        searchResultsList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SearchService.SearchResult selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openNoteFromResult(selected);
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hideSearchResults();
            }
        });

        // --- Handle mouse clicks on results ---
        searchResultsList.setOnMouseClicked(event -> {
            SearchService.SearchResult selected = searchResultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openNoteFromResult(selected);
            }
        });

        // --- Customize how each search result is displayed ---
        searchResultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SearchService.SearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Display: "Set Name > Note Title" on one line
                    // And "Match at character: 123" on the second
                    Label titleLabel = new Label(item.setName() + "  ›  " + item.title());
                    titleLabel.getStyleClass().add("result-title");

                    Label positionLabel = new Label("Match at character: " + item.position());
                    positionLabel.getStyleClass().add("result-set-name"); // Using existing style for smaller text

                    VBox contentBox = new VBox(titleLabel, positionLabel);
                    contentBox.setSpacing(2);
                    setGraphic(contentBox);
                }
            }
        });
    }

    private void performSearch(String query) {
        // Perform search on a background thread to keep UI responsive
        new Thread(() -> {
            try {
                var results = EurekaApp.getSearchService().search(query);
                Platform.runLater(() -> {
                    if (results.isEmpty() && !query.isEmpty()) {
                        // Show a "no results" message or just hide
                        hideSearchResults();
                    } else {
                        searchResultsList.setItems(FXCollections.observableArrayList(results));
                        showSearchResults();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(this::hideSearchResults);
            }
        }).start();
    }

    private void openNoteFromResult(SearchService.SearchResult result) {
        Optional<Note> noteOpt = AppState.getInstance().getNoteById(result.noteId());
        noteOpt.ifPresent(note -> {
            // Use the NEW listener method to pass the position
            noteSelectionListener.onNoteSelectedFromSearch(note, result.position(), result.query());
        });
        hideSearchResults();
    }

    private void showSearchResults() {
        if (rootPane == null || searchField.getScene() == null) return;
        backdrop.setVisible(true);
        searchResultsList.setVisible(true);
        // Position the list right below the search field
        double fieldSceneX = searchField.localToScene(0, 0).getX();
        double fieldSceneY = searchField.localToScene(0, searchField.getHeight()).getY();

        searchResultsList.setLayoutX(fieldSceneX);
        searchResultsList.setLayoutY(fieldSceneY + 5);
        searchResultsList.setPrefWidth(searchField.getWidth());
    }

    private void hideSearchResults() {
        if (rootPane == null) return;
        backdrop.setVisible(false);
        searchResultsList.setVisible(false);
        searchField.clear();
    }
}

