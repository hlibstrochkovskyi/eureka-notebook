package com.eureka.ui;

import com.eureka.I18n;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import java.util.prefs.Preferences;

public class ThemeManager {

    public enum Theme { LIGHT, DARK, SYSTEM }

    private static final String LIGHT_CSS = "/theme-light.css";
    private static final String DARK_CSS = "/theme-dark.css";
    private static final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(loadThemePreference());

    public static void initialize(Scene scene) {
        applyThemeToScene(scene);
        currentTheme.addListener((obs, oldTheme, newTheme) -> applyThemeToScene(scene));
    }

    public static ObjectProperty<Theme> currentThemeProperty() {
        return currentTheme;
    }

    public static Theme getCurrentTheme() {
        return currentTheme.get();
    }

    public static void setTheme(Theme theme) {
        saveThemePreference(theme);
        currentTheme.set(theme);
    }

    private static void applyThemeToScene(Scene scene) {
        // First, remove old theme stylesheets to prevent conflicts
        scene.getStylesheets().remove(ThemeManager.class.getResource(LIGHT_CSS).toExternalForm());
        scene.getStylesheets().remove(ThemeManager.class.getResource(DARK_CSS).toExternalForm());

        Theme effectiveTheme = resolveEffectiveTheme(currentTheme.get());

        String cssPath = (effectiveTheme == Theme.DARK) ? DARK_CSS : LIGHT_CSS;
        scene.getStylesheets().add(0, ThemeManager.class.getResource(cssPath).toExternalForm());
    }

    private static Theme resolveEffectiveTheme(Theme theme) {
        if (theme == Theme.SYSTEM) {
            // Basic check for OS dark mode. This is a simplification.
            // A full implementation requires platform-specific code.
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win") || os.contains("mac")) {
                // For simplicity, we assume system is light. A real implementation would query the OS.
                return Theme.LIGHT;
            }
        }
        return theme;
    }

    private static void saveThemePreference(Theme theme) {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        prefs.put("app_theme", theme.name());
    }

    private static Theme loadThemePreference() {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        String themeName = prefs.get("app_theme", Theme.SYSTEM.name());
        try {
            return Theme.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            return Theme.SYSTEM;
        }
    }
}