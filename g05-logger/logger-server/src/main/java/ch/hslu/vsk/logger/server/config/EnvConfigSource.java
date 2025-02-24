package ch.hslu.vsk.logger.server.config;

import java.nio.file.Path;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * A configuration source that retrieves values from environment variables.
 * Uses the {@link Dotenv} library to load environment variables from a `.env` file or the system environment.
 */
public final class EnvConfigSource implements ConfigSource {

    /**
     * The {@link Dotenv} instance used to access environment variables.
     */
    private final Dotenv dotenv;

    /**
     * Constructs an {@code EnvConfigSource} with a specified path to the `.env` file.
     * If the specified file is not found, the configuration will fall back to the system environment.
     *
     * @param envFilePath The path to the `.env` file to load environment variables from.
     */
    public EnvConfigSource(final Path envFilePath) {
        this.dotenv = Dotenv.configure()
                            .ignoreIfMissing()
                            .directory(envFilePath.getParent().toString())
                            .filename(envFilePath.getFileName().toString())
                            .load();
    }

    /**
     * Constructs an {@code EnvConfigSource} that loads environment variables from the default `.env` file or
     * falls back to the system environment if the file is not found.
     */
    public EnvConfigSource() {
        this.dotenv = Dotenv.configure()
                            .ignoreIfMissing()
                            .load();
    }

    /**
     * Retrieves the value of a specified configuration key from the environment variables.
     * Converts hyphens in the key to underscores and converts the key to uppercase
     * to match typical environment variable naming conventions.
     *
     * @param key The key for the desired configuration value.
     * @return The value associated with the specified key, or {@code null} if the key is not found.
     */
    @Override
    public String getConfigValue(final String key) {
        return this.dotenv.get(key.replace("-", "_").toUpperCase());
    }
}
