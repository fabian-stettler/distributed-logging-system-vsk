package ch.hslu.vsk.logger.common.formatter;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

import java.time.Instant;
import java.time.format.DateTimeParseException;


/**
 * This class formats {@link LogMessage} objects into JSON and parses JSON strings back into {@link LogMessage} objects.
 * The JSON format provides a structured representation of log messages that can be easily read by systems and humans.
 */
public class JSONLogMessageFormatter implements LogMessageFormatter {

	/**
	 * Creates a new formatter that can convert {@link LogMessage}s to JSON and convert them back
	 */
	public JSONLogMessageFormatter() {
		// No initialization is necessary for this formatter.
	}

    /**
     * Serializes a {@link LogMessage} object into a JSON-formatted string.
     * The JSON format includes fields for:
     * <ul>
     *     <li>receivedAt: The timestamp when the message was received at the server.</li>
     *     <li>timestamp: The original timestamp of the log message.</li>
     *     <li>clientName: The name of the client that sent the log message.</li>
     *     <li>logLevel: The log level (e.g., INFO, ERROR).</li>
     *     <li>message: The log message content.</li>
     * </ul>
     * 
     * @param message The {@link LogMessage} object to serialize.
     * @return A JSON-formatted string representation of the log message.
     */
    @Override
    public String serialize(final LogMessage message) {
        String receivedAtFormatted = InstantSerializer.stringifyInstant(message.getReceivedAtServer());
        String timestampFormatted = InstantSerializer.stringifyInstant(message.getTimestamp());
        String logLevel = message.getLogLevel().toString();
        String messageString = message.getMessage().replace("\"", "\\\"");
        return "{\"receivedAt\": \"" + receivedAtFormatted + "\", " +
               "\"timestamp\": \"" + timestampFormatted + "\", " +
               "\"clientName\": \"" + message.getClientName() + "\", " +
               "\"logLevel\": \"" + logLevel + "\", " +
               "\"message\": \"" + messageString + "\"}";
    }

    /**
     * Parses a JSON-formatted string into a {@link LogMessage} object.
     * The input string must contain the following attributes:
     * <ul>
     *     <li>receivedAt: The timestamp when the message was received at the server.</li>
     *     <li>timestamp: The original timestamp of the log message.</li>
     *     <li>clientName: The name of the client that sent the log message.</li>
     *     <li>logLevel: The log level (e.g., INFO, ERROR).</li>
     *     <li>message: The log message content.</li>
     * </ul>
     * 
     * If any of these attributes are missing, an {@link IllegalArgumentException} is thrown.
     * 
     * @param totalMessage The JSON-formatted string to parse.
     * @return A {@link LogMessage} object created from the parsed JSON string.
     * @throws IllegalArgumentException If any required fields are missing or if parsing fails.
     */
    @Override
    public LogMessage parse(final String totalMessage) {
        if (!checkIfContainsAttribute(totalMessage, "receivedAt")) {
            throw new IllegalArgumentException("The JSON string does not contain a receivedAt attribute.");
        }
        if (!checkIfContainsAttribute(totalMessage, "timestamp")) {
            throw new IllegalArgumentException("The JSON string does not contain a timestamp attribute.");
        }
        if (!checkIfContainsAttribute(totalMessage, "clientName")) {
            throw new IllegalArgumentException("The JSON string does not contain a clientName attribute.");
        }
        if (!checkIfContainsAttribute(totalMessage, "logLevel")) {
            throw new IllegalArgumentException("The JSON string does not contain a logLevel attribute.");
        }
        if (!checkIfContainsAttribute(totalMessage, "message")) {
            throw new IllegalArgumentException("The JSON string does not contain a message attribute.");
        }

        String receivedAtString = totalMessage.replaceAll(".*\"receivedAt\":\\s*\"([^\"]+)\".*", "$1");
        String timestampString = totalMessage.replaceAll(".*\"timestamp\":\\s*\"([^\"]+)\".*", "$1");
        String clientNameString = totalMessage.replaceAll(".*\"clientName\":\\s*\"([^\"]+)\".*", "$1");
        String logLevelString = totalMessage.replaceAll(".*\"logLevel\":\\s*\"([^\"]+)\".*", "$1");
        String messageString = totalMessage.replaceAll(".*\"message\":\\s*\"((?:\\\\\"|[^\"])+)\".*", "$1");

        messageString = messageString.replace("\\\"", "\"");

        try {
            Instant receivedAt = InstantSerializer.parseInstant(receivedAtString);
            Instant timestamp = InstantSerializer.parseInstant(timestampString);
            LogLevel logLevel = LogLevel.valueOf(logLevelString);

            return new LogMessage(receivedAt, timestamp, clientNameString, logLevel, messageString);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Failed to parse the receivedAt or timestamp fields", ex);
        }
    }

    /**
     * Checks if a JSON-formatted string contains a specific key.
     * 
     * @param json The JSON string to check.
     * @param key The key to check for.
     * @return True if the key is present in the JSON string, false otherwise.
     */
    private boolean checkIfContainsAttribute(String json, String key) {
        try {
            return json.contains(key);
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }
}
