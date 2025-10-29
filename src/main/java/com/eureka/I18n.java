package com.eureka;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Handles internationalization (i18n) for the application.
 * Loads localized strings from resource bundles based on the current locale.
 */
public final class I18n {

    private static final ObjectProperty<Locale> locale;

    static {
        locale = new SimpleObjectProperty<>(getDefaultLocale());
        locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
    }

    /**
     * Gets the default locale (you might want to load this from preferences).
     * For now, it defaults to English.
     * @return The default Locale.
     */
    private static Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }

    /**
     * Gets the current locale property.
     * @return The ObjectProperty containing the current Locale.
     */
    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    /**
     * Gets the currently set locale.
     * @return The current Locale.
     */
    public static Locale getLocale() {
        return locale.get();
    }

    /**
     * Sets the application's locale.
     * @param locale The new Locale to set.
     */
    public static void setLocale(Locale locale) {
        Locale.setDefault(locale);
        I18n.locale.set(locale);
        System.out.println("Locale changed to: " + locale.getLanguage());
    }

    /**
     * Gets the localized string for the given key.
     * Uses UTF8Control to ensure correct character encoding.
     * @param key The key for the desired string in the resource bundle.
     * @param args Optional arguments for message formatting.
     * @return The localized string.
     */
    public static String get(final String key, final Object... args) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", getLocale(), new UTF8Control());
            return String.format(bundle.getString(key), args);
        } catch (MissingResourceException e) {
            System.err.println("Warning: Missing resource key: " + key + " for locale " + getLocale());
            return "!" + key + "!";
        } catch (Exception e) {
            System.err.println("Error getting resource for key: " + key);
            e.printStackTrace();
            return "?" + key + "?";
        }
    }

    /**
     * Creates a StringBinding that automatically updates when the locale changes.
     * @param key The key for the desired string.
     * @param args Optional arguments for message formatting.
     * @return A StringBinding for the localized string.
     */
    public static StringBinding bind(final String key, final Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), locale);
    }

    private I18n() {}
}