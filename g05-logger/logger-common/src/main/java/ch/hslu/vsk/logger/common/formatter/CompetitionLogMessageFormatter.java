package ch.hslu.vsk.logger.common.formatter;

import java.util.Locale;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

import java.time.Instant;

/**
 * A formatter for log messages in a specific format used in competitions.
 * This formatter serializes log messages into a string and can parse strings back into {@link LogMessage} objects.
 */
public class CompetitionLogMessageFormatter implements LogMessageFormatter {

	
	/**
	 * Serializes a {@link LogMessage} into a human-readable string.
	 * The format used is: 
	 * - Timestamp (ISO-8601 format)
	 * - Log level (upper case)
	 * - Client name
	 * - Log message content
	 * 
	 * @param message The {@link LogMessage} to serialize
	 * @return A formatted string representation of the log message
	 */
	@Override
	public String serialize(LogMessage message) {
		String timestamp = InstantSerializer.stringifyInstant(message.getTimestamp());

		// Use a StringBuilder with an estimated capacity to avoid resizing
		StringBuilder builder = new StringBuilder(timestamp.length() + 
			message.getLogLevel().toString().length() + 
			message.getClientName().length() + 
			message.getMessage().length() + 4 // for the spaces
		);

		builder.append(timestamp)
		   	.append(' ')
		   	.append(message.getLogLevel().name())
		   	.append(' ')
		   	.append(message.getClientName())
		   	.append(' ')
		   	.append(message.getMessage());

		return builder.toString();
	}


	/**
	 * Parses a formatted log message string back into a {@link LogMessage} object.
	 * The expected format is:
	 * - Timestamp (ISO-8601 format)
	 * - Log level (upper case)
	 * - Client name
	 * - Log message content
	 * 
	 * @param message The string to parse into a {@link LogMessage}
	 * @return The parsed {@link LogMessage} object
	 * @throws IllegalArgumentException If the message format is invalid
	 */
	@Override
	public LogMessage parse(String message) {
		// Example of a message format: "2024-12-05 10:15:30.1234 INFO Client1 Test message"
		try {
			// First, split the message into parts based on the space
			String[] parts = message.split(" ", 5);
			if (parts.length != 5) {
				throw new IllegalArgumentException("Message format is incorrect.");
			}

			// Parse the timestamp part (first part of the string)
			String timestampString = parts[0] + " " + parts[1]; // Combine date and time part (yyyy-MM-dd HH:mm:ss.SSSS)
			Instant timestamp = InstantSerializer.parseInstant(timestampString);

			// Parse the log level part (second part of the string)
			String logLevelString = parts[2];
			LogLevel logLevel = LogLevel.valueOf(logLevelString.toUpperCase(Locale.getDefault())); // Convert to uppercase to match enum

			// Parse the client name (third part of the string)
			String clientName = parts[3]; // Extract client name
			String messageContent = parts[4]; // Extract message content

			// Create and return the LogMessage
			return new LogMessage(timestamp, clientName, logLevel, messageContent);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse log message.", e);
		}
	}

}
