package com.eureka.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {
    public enum Theme { LIGHT, DARK }

    // File paths for the stylesheets
    private static final String LIGHT_CSS = "/light.css";
    private static final String DARK_CSS = "/dark.css";

    private static final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(loadThemePreference());

    public static void initialize(Scene scene) {
        // Add a listener to change the stylesheet when the theme property changes
        currentTheme.addListener((obs, oldTheme, newTheme) -> applyTheme(scene, newTheme));
        // Apply the initial theme when the app starts
        applyTheme(scene, currentTheme.get());
    }

    private static void applyTheme(Scene scene, Theme theme) {
        // Remove all previous theme stylesheets to prevent conflicts
        scene.getStylesheets().remove(LIGHT_CSS);
        scene.getStylesheets().remove(DARK_CSS);

        // Add the correct stylesheet based on the selected theme
        String cssPath = (theme == Theme.DARK) ? DARK_CSS : LIGHT_CSS;
        scene.getStylesheets().add(cssPath);
    }

    public static ObjectProperty<Theme> currentThemeProperty() { return currentTheme; }
    public static Theme getCurrentTheme() { return currentTheme.get(); }

    public static void setTheme(Theme theme) {
        saveThemePreference(theme);
        currentTheme.set(theme);
    }

    private static void saveThemePreference(Theme theme) {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        prefs.put("app_theme", theme.name());
    }

    private static Theme loadThemePreference() {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        String themeName = prefs.get("app_theme", Theme.LIGHT.name());
        try {
            return Theme.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            return Theme.LIGHT; // Fallback to light theme
        }
    }
}