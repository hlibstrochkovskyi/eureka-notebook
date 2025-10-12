package com.eureka;

import com.eureka.model.AppState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DataStorageService {
    private static final String FILE_PATH = "eureka_data.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveData(AppState appState) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(appState, writer);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving data to file: " + FILE_PATH);
            e.printStackTrace();
        }
    }

    public static AppState loadData() {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            AppState loadedState = gson.fromJson(reader, AppState.class);
            System.out.println("Data loaded successfully.");
            return loadedState != null ? loadedState : AppState.createEmptyState();
        } catch (IOException e) {
            System.err.println("No existing data file found. Starting with a fresh state.");
            return AppState.createEmptyState();
        }
    }
}