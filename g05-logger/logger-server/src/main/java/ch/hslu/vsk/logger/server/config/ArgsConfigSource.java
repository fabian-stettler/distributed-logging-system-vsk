package ch.hslu.vsk.logger.server.config;

/**
 * Parses command-line arguments into readable configuration values.
 * This implementation converts arguments in the form of {@code --key=value}
 * into key-value pairs for configuration purposes.
 */
public final class ArgsConfigSource implements ConfigSource {

    /**
     * The array of command-line arguments to be parsed.
     */
    private final String[] args;

    /**
     * Constructs an {@code ArgsConfigSource} with the provided array of arguments.
     *
     * @param args The array of command-line arguments, typically passed to the application on startup.
     */
    public ArgsConfigSource(final String[] args) {
        this.args = args;
    }

    /**
     * Retrieves the value associated with a specified configuration key.
     * The key is matched in the format {@code --key=value}, where underscores
     * in the key are replaced with hyphens.
     *
     * @param key The configuration key to retrieve, with underscores as separators.
     * @return The value associated with the specified key, or {@code null} if the key is not present.
     */
    @Override
    public String getConfigValue(final String key) {
        String parsedKey = "--" + key.replace("_", "-");
        for (String arg : args) {
            if (arg.startsWith(parsedKey + "=")) {
                return arg.split("=", 2)[1];
            }
        }
        return null;
    }
}
