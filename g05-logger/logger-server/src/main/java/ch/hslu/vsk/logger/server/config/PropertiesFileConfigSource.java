package ch.hslu.vsk.logger.server.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A configuration source that retrieves values from a properties file.
 * Keys are expected to follow the format of typical Java `.properties` files, and values are retrieved by their keys.
 */
public final class PropertiesFileConfigSource implements ConfigSource {

    /**
     * Logger for logging errors and informational messages.
     */
    private static final Logger LOGGER = LogManager.getLogger(PropertiesFileConfigSource.class);

    /**
     * The {@link Properties} instance used to store the loaded key-value pairs.
     */
    private final Properties properties = new Properties();

    /**
     * Constructs a {@code PropertiesFileConfigSource} and loads properties from the specified file.
     * If the file cannot be read, an error is logged.
     *
     * @param filePath The path to the properties file to be loaded.
     */
    public PropertiesFileConfigSource(final String filePath) {
        try (FileInputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException ex) {
            LOGGER.error("Failed to read from properties file", ex);
        }
    }

    /**
     * Retrieves the value of a configuration key from the loaded properties.
     * Converts hyphens in the key to underscores and converts the key to uppercase
     * to allow for flexible key formats.
     *
     * @param key The key for the desired configuration value.
     * @return The value associated with the specified key, or {@code null} if the key is not found.
     */
    @Override
    public String getConfigValue(final String key) {
        return this.properties.getProperty(key.replace("-", "_").toUpperCase());
    }
}
