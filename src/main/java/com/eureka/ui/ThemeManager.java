package com.eureka.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;

import java.util.prefs.Preferences;

/**
 * Manages the application's theme (Light/Dark).
 * Based on the implementation details from FEAT-007.
 */
public class ThemeManager {

    public enum Theme {
        LIGHT, DARK
    }

    private static final String LIGHT_THEME_CSS = "/theme-light.css";
    private static final String DARK_THEME_CSS = "/theme-dark.css";
    private static final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(loadThemePreference());

    /**
     * Initializes the theme manager and applies the saved or default theme to the scene.
     * @param scene The main application scene.
     */
    public static void initialize(Scene scene) {
        applyThemeToScene(scene);
        currentTheme.addListener((obs, oldTheme, newTheme) -> {
            saveThemePreference(newTheme);
            applyThemeToScene(scene);
        });
    }

    public static void applyTheme(Theme theme) {
        currentTheme.set(theme);
    }

    private static void applyThemeToScene(Scene scene) {
        // Remove all theme stylesheets before applying a new one
        scene.getStylesheets().removeAll(
                ThemeManager.class.getResource(LIGHT_THEME_CSS).toExternalForm(),
                ThemeManager.class.getResource(DARK_THEME_CSS).toExternalForm()
        );

        String cssPath = (currentTheme.get() == Theme.DARK) ? DARK_THEME_CSS : LIGHT_THEME_CSS;
        scene.getStylesheets().add(ThemeManager.class.getResource(cssPath).toExternalForm());
    }

    private static void saveThemePreference(Theme theme) {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        prefs.put("app_theme", theme.name());
    }

    public static Theme loadThemePreference() {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        String themeName = prefs.get("app_theme", Theme.LIGHT.name());
        return Theme.valueOf(themeName);
    }
}