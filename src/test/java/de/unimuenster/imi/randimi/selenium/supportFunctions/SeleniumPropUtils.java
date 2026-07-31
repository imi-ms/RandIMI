package de.unimuenster.imi.randimi.selenium.supportFunctions;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Log4j2
public final class SeleniumPropUtils {

    private static final Map<String, Properties> propertiesCache = new HashMap<>();

    /**
     * Reads the given property name from the file with the given name.
     * @param propertyName The property to be read.
     * @param fileName The properties file.
     * @return The value.
     */
    public static String getProperty(final String propertyName, final String fileName) {
        if (!propertiesCache.containsKey(fileName)) {
            try (InputStream input = SeleniumPropUtils.class.getClassLoader().getResourceAsStream(fileName)) {
                if(input == null) {
	                log.error("Could not find file {}", fileName);
                };

                final Properties prop = new Properties();
                prop.load(input);
                propertiesCache.put(fileName, prop);
            } catch (final IOException ex) {
                log.error("Failed to load properties", ex);
            }
        }

        return propertiesCache.get(fileName).getProperty(propertyName);
    }

    /**
     * Fetches the requested property from the selenium.properties file.
     * @param propertyName Name of the requested property value.
     * @return The value of the requested property.
     */
    public static String getProperty(final String propertyName) {
        return getProperty(propertyName, "selenium.properties");
    }
}
