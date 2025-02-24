package ch.hslu.vsk.logger.common.formatter;

import java.time.DateTimeException;
import java.time.Instant;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

/**
 * A formatter for log messages that converts them to and from a human-readable format.
 * This formatter represents log message fields in a structured text format with clear labels
 * and values, making it easy to read and debug.
 */
public final class HumanReadableLogMessageFormatter implements LogMessageFormatter {

    private static final String RECEIVED_AT_LABEL = "ReceivedAt: [";
    private static final String TIMESTAMP_LABEL = "Timestamp: [";
    private static final String CLIENT_NAME_LABEL = "Client-Name: [";
    private static final String LOG_LEVEL_LABEL = "Log-Level: [";
    private static final String MESSAGE_LABEL = "Message: [";
	/**
	 * Creates a new formatter that can convert {@link LogMessage}s to a human readable format and convert them back
	 */
	public HumanReadableLogMessageFormatter() {
		// No initialization is necessary for this formatter.
	}

    /**
     * Serializes a {@link LogMessage} object into a human-readable string format.
     * Each field of the log message is clearly labeled and enclosed in square brackets.
     * Fields are separated by newlines.
     * 
     * @param message The {@link LogMessage} object to serialize.
     * @return A human-readable string representation of the log message.
     * @throws IllegalArgumentException If the {@code LogMessage} is null.
     */
    @Override
    public String serialize(final LogMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("LogMessage cannot be null");
        }

        return RECEIVED_AT_LABEL + InstantSerializer.stringifyInstant(message.getReceivedAtServer()) + "]\n" +
                TIMESTAMP_LABEL + InstantSerializer.stringifyInstant(message.getTimestamp()) + "]\n" +
                CLIENT_NAME_LABEL + message.getClientName() + "]\n" +
                LOG_LEVEL_LABEL + message.getLogLevel() + "]\n" +
                MESSAGE_LABEL + message.getMessage() + "]";
    }

    /**
     * Parses a human-readable log message string back into a {@link LogMessage} object.
     * The input string must contain all expected fields, each clearly labeled and formatted as:
     * <pre>
     * ReceivedAt: [timestamp]
     * Timestamp: [timestamp]
     * Client-Name: [client-name]
     * Log-Level: [log-level]
     * Message: [message]
     * </pre>
     * 
     * @param message The human-readable log message string to parse.
     * @return A {@link LogMessage} object created from the parsed string.
     * @throws IllegalArgumentException If the input string is missing any required fields 
     *                                  or if timestamps or log levels are invalid.
     */
    @Override
    public LogMessage parse(final String message) {
        // Check if all fields are present
        if (!message.contains(RECEIVED_AT_LABEL) ||
            !message.contains(TIMESTAMP_LABEL) ||
            !message.contains(CLIENT_NAME_LABEL) ||
            !message.contains(LOG_LEVEL_LABEL) ||
            !message.contains(MESSAGE_LABEL)) {
            throw new IllegalArgumentException("The log message does not contain all required fields.");
        }

        // Extract field values from the input string
        String receivedAtServerTimestampString = message.substring(
            message.indexOf(RECEIVED_AT_LABEL) + 13, message.indexOf("]\nTimestamp:"));
        String timestampString = message.substring(
            message.indexOf(TIMESTAMP_LABEL) + 12, message.indexOf("]\nClient-Name:"));
        String clientNameString = message.substring(
            message.indexOf(CLIENT_NAME_LABEL) + 14, message.indexOf("]\nLog-Level:"));
        String logLevelString = message.substring(
            message.indexOf(LOG_LEVEL_LABEL) + 12, message.indexOf("]\nMessage:"));
        String messageString = message.substring(
            message.indexOf(MESSAGE_LABEL) + 10, message.length() - 1);

        try {
            // Parse field values into appropriate data types
            Instant receivedAtServerTimestamp = InstantSerializer.parseInstant(receivedAtServerTimestampString);
            Instant timeStamp = InstantSerializer.parseInstant(timestampString);
            LogLevel logLevel = LogLevel.valueOf(logLevelString);

            // Create and return a new LogMessage object
            return new LogMessage(receivedAtServerTimestamp, timeStamp, clientNameString, logLevel, messageString);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Invalid timestamp format", ex);
        }
    }
}
