package ch.hslu.vsk.logger.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A utility class for serializing and deserializing {@link Instant} objects
 * to and from a specific string format. The format used is "yyyy-MM-dd HH:mm:ss.SSS"
 * in the UTC timezone.
 * <p>
 * This class is thread-safe as it uses an immutable {@link DateTimeFormatter}.
 */
public final class InstantSerializer {

    /**
     * A thread-safe {@link DateTimeFormatter} for formatting and parsing {@link Instant} objects.
     * The formatter uses the pattern "yyyy-MM-dd HH:mm:ss.SSSS" in the UTC timezone.
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSS")
            .withZone(ZoneId.of("UTC"));

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private InstantSerializer() {
        // Prevent instantiation
    }

    /**
     * Converts an {@link Instant} to its string representation using the specified format.
     *
     * @param instant the {@link Instant} to serialize; can be null.
     * @return the formatted string representation of the {@link Instant}, or "NULL" if the input is null.
     */
    public static String stringifyInstant(final Instant instant) {
		if (instant == null) {
			return "NULL";
		}

		return TIMESTAMP_FORMATTER.format(instant);
    }

    /**
     * Parses a string representation of an {@link Instant} back into an {@link Instant} object.
     *
     * @param serialized the string representation of the {@link Instant}; can be "NULL" or a valid formatted string.
     * @return the parsed {@link Instant}, or null if the input is "NULL" or if parsing fails.
     */
    public static Instant parseInstant(final String serialized) {
        if ("NULL".equals(serialized)) {
            return null;
        }
        try {
            return Instant.from(TIMESTAMP_FORMATTER.parse(serialized));
        } catch (DateTimeParseException ex) {
            return null; // Parsing failed, return null
        }
    }
}

