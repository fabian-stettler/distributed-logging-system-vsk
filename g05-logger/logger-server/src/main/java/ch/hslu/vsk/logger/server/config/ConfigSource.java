package ch.hslu.vsk.logger.server.config;

/**
 * Represents a source for configuration values.
 * Implementations of this interface provide mechanisms to retrieve configuration values
 * from various sources such as environment variables, command-line arguments, files, etc.
 */
public interface ConfigSource {

    /**
     * Retrieves the configuration value associated with the specified key.
     *
     * @param key The key for which the configuration value is to be retrieved.
     * @return The value associated with the specified key, or {@code null} if the key is not found.
     */
    String getConfigValue(String key);
}

