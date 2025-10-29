package com.eureka;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Custom {@link ResourceBundle.Control} implementation that ensures
 * {@code .properties} resource bundles are loaded using UTF-8 encoding.
 * This is necessary to correctly display characters from various languages
 * that are not part of the standard ISO-8859-1 encoding used by default
 * by {@link PropertyResourceBundle}.
 */
public class UTF8Control extends ResourceBundle.Control {

    /**
     * Loads a {@link ResourceBundle} from a {@code .properties} file using UTF-8 encoding.
     * This method overrides the default behavior to specify {@code StandardCharsets.UTF_8}
     * when creating the {@link InputStreamReader}. It handles reloading logic
     * similar to the default implementation.
     *
     * @param baseName the base name of the resource bundle, a fully qualified class name
     * @param locale   the locale for which the resource bundle should be loaded
     * @param format   the resource bundle format to be loaded (expected to be "java.properties")
     * @param loader   the ClassLoader to use for loading the resource
     * @param reload   true if reloading the bundle should be attempted; otherwise false
     * @return the loaded {@link ResourceBundle}, or null if the resource could not be found
     * @throws IllegalAccessException if the class or initializer is not accessible
     * @throws InstantiationException if the instantiation of a class fails for some other reason
     * @throws IOException            if an error occurred when reading resources
     */
    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {

        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");

        ResourceBundle bundle = null;
        InputStream stream = null;
        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }

        if (stream != null) {
            try {
                bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
            } finally {
                stream.close();
            }
        }
        return bundle;
    }
}