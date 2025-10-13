package com.eureka;

import com.eureka.model.AppState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataStorageService {
    private static final String FILE_PATH = "eureka_data.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveData(AppState appState) {
        // Use Files.newBufferedWriter to ensure UTF-8 encoding
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(appState, writer);
            System.out.println("Data saved successfully in UTF-8.");
        } catch (IOException e) {
            System.err.println("Error saving data to file: " + FILE_PATH);
            e.printStackTrace();
        }
    }

    public static AppState loadData() {
        // Use Files.newBufferedReader to ensure UTF-8 encoding
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(FILE_PATH), StandardCharsets.UTF_8)) {
            AppState loadedState = gson.fromJson(reader, AppState.class);
            System.out.println("Data loaded successfully in UTF-8.");
            return loadedState != null ? loadedState : AppState.createEmptyState();
        } catch (IOException e) {
            System.err.println("No existing data file found. Starting with a fresh state.");
            return AppState.createEmptyState();
        }
    }
}