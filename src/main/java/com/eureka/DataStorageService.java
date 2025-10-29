package com.eureka;

import com.eureka.model.AppState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles saving and loading the application state (AppState) to and from a JSON file.
 * Uses Google's Gson library for serialization and deserialization.
 * Ensures UTF-8 encoding for file operations.
 */
public class DataStorageService {
    /**
     * The name of the file used to store the application data.
     */
    private static final String FILE_PATH = "eureka_data.json";
    /**
     * Static Gson instance configured for pretty printing JSON output.
     * Used for both saving and loading.
     */
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private DataStorageService() {}

    /**
     * Saves the provided AppState object to a JSON file (eureka_data.json).
     * Serializes the AppState using Gson and writes it to the file using UTF-8 encoding.
     * Prints success or error messages to the console.
     * @param appState The AppState object to save.
     */
    public static void saveData(AppState appState) {
        Path filePath = Paths.get(FILE_PATH);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            gson.toJson(appState, writer);
            System.out.println("Data saved successfully in UTF-8 to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving data to file: " + filePath.toAbsolutePath());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error during JSON serialization to file: " + filePath.toAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Loads the AppState object from the JSON file (eureka_data.json).
     * Deserializes the JSON content using Gson and UTF-8 encoding.
     * If the file doesn't exist or an error occurs during loading/parsing,
     * it returns a new, empty AppState.
     * Prints status messages to the console.
     * @return The loaded AppState object, or a new empty AppState if loading fails.
     */
    public static AppState loadData() {
        Path filePath = Paths.get(FILE_PATH);
        if (!Files.exists(filePath)) {
            System.err.println("No existing data file found at: " + filePath.toAbsolutePath() + ". Starting with a fresh state.");
            return AppState.createEmptyState();
        }

        // Use try-with-resources to ensure the reader is closed automatically.
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            AppState loadedState = gson.fromJson(reader, AppState.class);
            System.out.println("Data loaded successfully in UTF-8 from: " + filePath.toAbsolutePath());
            return loadedState != null ? loadedState : AppState.createEmptyState();
        } catch (IOException e) {
            System.err.println("Error reading data file: " + filePath.toAbsolutePath() + ". Starting with a fresh state.");
            e.printStackTrace();
            return AppState.createEmptyState();
        } catch (Exception e) {
            System.err.println("Error parsing JSON data from file: " + filePath.toAbsolutePath() + ". Starting with a fresh state.");
            e.printStackTrace();
            return AppState.createEmptyState();
        }
    }
}