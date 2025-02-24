package ch.hslu.vsk.logger.common.formatter;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * This class formats log messages as quickly as possible.
 * The actual format is not important, as long as the formatting process is fast.
 * Three pipe symbols ("|||") are used as delimiters to ensure speed while still being resistant to errors.
 */
public class SpeedFormatter implements LogMessageFormatter {

    private static final String DELIMITER = " ||| ";
	/**
	 * Creates a new formatter that can serialize and deserialize {@link LogMessage}s fast.
	 */
	public SpeedFormatter() {
		// No initialization is necessary for this formatter.
	}

    /**
     * Serializes a {@code LogMessage} into a string representation.
     * The format includes the following fields: ReceivedAt, Timestamp, Client-Name, Log-Level, and Message,
     * with each field separated by " ||| ".
     *
     * @param message The {@code LogMessage} to be serialized.
     * @return A string representation of the log message.
     */
    @Override
    public String serialize(final LogMessage message) {
        String receivedAtFormatted = InstantSerializer.stringifyInstant(message.getReceivedAtServer());
        String timestampFormatted = InstantSerializer.stringifyInstant(message.getTimestamp());
        return "ReceivedAt: " + receivedAtFormatted + DELIMITER +"Timestamp: " + timestampFormatted + DELIMITER + "Client-Name: " + message.getClientName() + DELIMITER + "Log-Level: " + message.getLogLevel() + DELIMITER + "Message: " + message.getMessage();
    }

    /**
     * Parses a log message string and converts it into a {@code LogMessage} object.
     * The format of the message is expected to be:
     * "ReceivedAt: value ||| Timestamp: value ||| Client-Name: value ||| Log-Level: value ||| Message: value".
     * The method throws an {@code IllegalArgumentException} if the format is invalid or if there are parsing errors.
     *
     * @param message The string representation of the log message to be parsed.
     * @return The corresponding {@code LogMessage} object.
     * @throws IllegalArgumentException If the message format is invalid or if parsing fails.
     */
    @Override
    public LogMessage parse(final String message) {
        int receivedAtIndex = getIndexOfField("ReceivedAt", message);
        int timestampIndex = getIndexOfField("Timestamp", message);
        int clientNameIndex = getIndexOfField("Client-Name", message);
        int logLevelIndex = getIndexOfField("Log-Level", message);
        int messageIndex = getIndexOfField("Message", message);

        try {
            String receivedAtServer = message.substring(receivedAtIndex, message.indexOf(DELIMITER, receivedAtIndex));
            String timestampStr = message.substring(timestampIndex, message.indexOf(DELIMITER, timestampIndex));
            String clientName = message.substring(clientNameIndex, message.indexOf(DELIMITER, clientNameIndex));
            String logLevel = message.substring(logLevelIndex, message.indexOf(DELIMITER, logLevelIndex));
            String logMessage = message.substring(messageIndex);

            Instant receivedAt = InstantSerializer.parseInstant(receivedAtServer);
            Instant timestamp = InstantSerializer.parseInstant(timestampStr);

            return new LogMessage(receivedAt, timestamp, clientName, LogLevel.valueOf(logLevel), logMessage);
        } catch (StringIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("Invalid format", ex);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format", ex);
        }
    }

    /**
     * Finds the starting index of a specific field within a log message string.
     * This method searches for the field name followed by ": " and returns the index where the value of the field begins.
     *
     * @param field The name of the field to find.
     * @param message The log message string.
     * @return The index of where the field value begins.
     * @throws IllegalArgumentException If the field is not found in the message.
     */
    private int getIndexOfField(final String field, final String message) {
        int index = message.indexOf(field + ": ");
        if (index == -1) {
            throw new IllegalArgumentException("Invalid format. Missing " + field);
        }
        return index + field.length() + 2;
    }
}
