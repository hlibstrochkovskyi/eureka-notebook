// src/main/java/com/eureka/ui/ThemeManager.java
package com.eureka.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import java.net.URL; // Make sure this import is present
import java.util.prefs.Preferences;

public class ThemeManager {
    public enum Theme { LIGHT, DARK }

    private static final String LIGHT_CSS = "/light.css";
    private static final String DARK_CSS = "/dark.css";
    private static final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(loadThemePreference());

    public static void initialize(Scene scene) {
        currentTheme.addListener((obs, oldTheme, newTheme) -> applyTheme(scene, newTheme));
        applyTheme(scene, currentTheme.get());
    }

    private static void applyTheme(Scene scene, Theme theme) {
        // First, clear all existing stylesheets to prevent conflicts
        scene.getStylesheets().clear();

        String cssPath = (theme == Theme.DARK) ? DARK_CSS : LIGHT_CSS;

        // THE FIX: Use the class's resource loader to get a full, valid URL to the CSS file.
        // This is the most reliable way to load resources.
        URL cssUrl = ThemeManager.class.getResource(cssPath);

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("Successfully loaded stylesheet: " + cssPath);
        } else {
            System.err.println("CRITICAL ERROR: Cannot find CSS file in resources: " + cssPath);
        }
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
            return Theme.LIGHT;
        }
    }
}