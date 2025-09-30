package com.eureka;

import com.eureka.model.AppState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataStorageService {
    private static final Logger LOGGER = Logger.getLogger(DataStorageService.class.getName());
    private static final Path FILE_PATH = Paths.get("eureka_data.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveData(AppState appState) {
        try {
            ensureParentDirectoryExists();
            try (Writer writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8)) {
                gson.toJson(appState, writer);
                LOGGER.info("Data saved successfully to " + FILE_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving data to file: " + FILE_PATH.toAbsolutePath(), e);
        }
    }

    public static AppState loadData() {
        if (!Files.exists(FILE_PATH)) {
            LOGGER.info("No existing data file found. Starting with a fresh state.");
            return AppState.createEmptyState();
        }

        try (Reader reader = Files.newBufferedReader(FILE_PATH, StandardCharsets.UTF_8)) {
            AppState loadedState = gson.fromJson(reader, AppState.class);
            if (loadedState == null) {
                LOGGER.warning("Loaded data was empty or malformed. Starting with a fresh state.");
                return AppState.createEmptyState();
            }
            LOGGER.info("Data loaded successfully from " + FILE_PATH.toAbsolutePath());
            return loadedState;
        } catch (JsonParseException e) {
            LOGGER.log(Level.SEVERE, "Data file is corrupted. Falling back to a fresh state: " + FILE_PATH.toAbsolutePath(), e);
            return AppState.createEmptyState();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading data from file: " + FILE_PATH.toAbsolutePath(), e);
            return AppState.createEmptyState();
        }
    }

    private static void ensureParentDirectoryExists() throws IOException {
        Path parent = FILE_PATH.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}