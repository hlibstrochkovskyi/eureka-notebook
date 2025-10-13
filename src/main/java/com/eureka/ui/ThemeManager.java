package com.eureka.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {
    public enum Theme { LIGHT, DARK }

    private static final String LIGHT_CSS = "/light.css";
    private static final String DARK_CSS = "/dark.css";
    private static final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(loadThemePreference());

    public static void initialize(Scene scene) {
        Parent root = scene.getRoot();
        currentTheme.addListener((obs, oldTheme, newTheme) -> updateStyleClass(root, newTheme));
        updateStyleClass(root, currentTheme.get()); // Apply initial theme
    }

    private static void updateStyleClass(Parent node, Theme theme) {
        // Remove both classes to be safe before adding the correct one
        node.getStyleClass().remove("dark");

        if (theme == Theme.DARK) {
            node.getStyleClass().add("dark");
        }
    }

    public static ObjectProperty<Theme> currentThemeProperty() { return currentTheme; }
    public static Theme getCurrentTheme() { return currentTheme.get(); }

    public static void setTheme(Theme theme) {
        saveThemePreference(theme);
        currentTheme.set(theme);
    }

    // --- Here is the full implementation of the missing methods ---

    private static void saveThemePreference(Theme theme) {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        prefs.put("app_theme", theme.name());
    }

    private static Theme loadThemePreference() {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        // Default to LIGHT if no preference is saved
        String themeName = prefs.get("app_theme", Theme.LIGHT.name());
        try {
            return Theme.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            // If the saved value is corrupted, fallback to LIGHT
            return Theme.LIGHT;
        }
    }
}