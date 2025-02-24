package ch.hslu.vsk.logger.common.formatter;

/**
 * Factory class for creating instances of {@code LogMessageFormatter}.
 * This class provides different formatter implementations based on the specified format type.
 *
 * This class cannot be instantiated.
 */
public final class LogMessageFormatterFactory {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private LogMessageFormatterFactory() {
        // Prevent instantiation
    }

    /**
     * Creates a {@code LogMessageFormatter} instance based on the specified {@code LogFormatterTypes}.
     *
     * @param type the format type for which a formatter instance is to be created.
     *             Must be one of the values defined in {@code LogFormatterTypes}.
     * @return a {@code LogMessageFormatter} instance corresponding to the specified type.
     * @throws IllegalArgumentException if the specified format type is not supported.
     */
    public static LogMessageFormatter createFormatter(final LogMessageFormat type) {
        if (type == null) {
            throw new IllegalArgumentException("LogFormatterTypes cannot be null");
        }

        // Return the correct formatter based on the type
        return switch (type) {
            case JSON -> new JSONLogMessageFormatter();
            case HUMAN_READABLE -> new HumanReadableLogMessageFormatter();
            case SPEED_FORMAT -> new SpeedFormatter();
            case XML -> new XMLLogMessageFormatter();
			case COMPETITION -> new CompetitionLogMessageFormatter();
            default -> throw new IllegalArgumentException("Unsupported log type: " + type);
        };
    }
}
