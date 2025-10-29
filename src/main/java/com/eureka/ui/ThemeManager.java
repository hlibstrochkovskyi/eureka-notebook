package com.eureka.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import java.net.URL;
import java.util.prefs.Preferences;

/**
 * Manages the application's visual theme (Light/Dark).
 * Handles loading, applying, and saving the theme preference.
 * Provides a JavaFX property to observe theme changes.
 */
public class ThemeManager {

    /**
     * Enumeration defining the available themes.
     */
    public enum Theme { LIGHT, DARK }

    private static final String LIGHT_CSS = "/light.css";
    private static final String DARK_CSS = "/dark.css";

    /**
     * JavaFX property holding the currently active theme.
     * Initialized by loading the saved theme preference.
     * Allows other parts of the UI to bind to or listen for theme changes.
     */
    private static final ObjectProperty<Theme> currentTheme = new SimpleObjectProperty<>(loadThemePreference());

    /**
     * Initializes the ThemeManager for a given Scene.
     * Adds a listener to the currentTheme property to automatically apply theme changes
     * to the scene. Applies the initial theme.
     * @param scene The main application Scene to apply themes to.
     */
    public static void initialize(Scene scene) {
        currentTheme.addListener((obs, oldTheme, newTheme) -> applyTheme(scene, newTheme));
        applyTheme(scene, currentTheme.get());
    }

    /**
     * Applies the specified theme to the given Scene.
     * Clears existing stylesheets and adds the appropriate CSS file (light.css or dark.css)
     * based on the selected theme. Handles potential errors if CSS files are not found.
     * @param scene The Scene to which the theme stylesheet should be applied.
     * @param theme The Theme (LIGHT or DARK) to apply.
     */
    private static void applyTheme(Scene scene, Theme theme) {
        scene.getStylesheets().clear();

        String cssPath = (theme == Theme.DARK) ? DARK_CSS : LIGHT_CSS;

        URL cssUrl = ThemeManager.class.getResource(cssPath);

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("Successfully loaded stylesheet: " + cssPath);
        } else {
            System.err.println("CRITICAL ERROR: Cannot find CSS file in resources: " + cssPath);
        }
    }

    /**
     * Returns the JavaFX property that holds the current theme.
     * Allows binding UI elements or listeners to theme changes.
     * @return The ObjectProperty<Theme> representing the current theme.
     */
    public static ObjectProperty<Theme> currentThemeProperty() { return currentTheme; }

    /**
     * Gets the currently active theme value.
     * @return The current Theme (LIGHT or DARK).
     */
    public static Theme getCurrentTheme() { return currentTheme.get(); }

    /**
     * Sets the application theme.
     * Saves the preference and updates the currentTheme property,
     * which triggers the listener to apply the changes visually.
     * @param theme The new Theme to set.
     */
    public static void setTheme(Theme theme) {
        saveThemePreference(theme);
        currentTheme.set(theme);
    }

    /**
     * Saves the selected theme preference using the Java Preferences API.
     * The preference is stored per user for this specific class package.
     * @param theme The Theme to save.
     */
    private static void saveThemePreference(Theme theme) {
        Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
        prefs.put("app_theme", theme.name());
    }

    /**
     * Loads the saved theme preference using the Java Preferences API.
     * Defaults to LIGHT theme if no preference is found or if the saved value is invalid.
     * @return The loaded Theme, or Theme.LIGHT as a default.
     */
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