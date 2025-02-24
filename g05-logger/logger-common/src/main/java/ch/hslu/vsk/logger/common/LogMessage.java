package ch.hslu.vsk.logger.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.network.NetworkMessageInputStream;
import ch.hslu.vsk.logger.common.network.NetworkMessageOutputStream;
import ch.hslu.vsk.logger.common.network.NetworkTransferable;
import ch.hslu.vsk.logger.common.util.EnumSerializer;

/**
 * Represents a log message that can be transmitted over the network between client and server.
 * This class encapsulates details such as timestamps, client information, log levels, and the message content.
 * It implements {@link NetworkTransferable} to facilitate serialization and deserialization for network transmission.
 */
public final class LogMessage implements NetworkTransferable {

	/**
	 * The unique identifier for serialization.
	 */
	public static final short serialVersionUID = 1;

	/**
	 * The serializer used to serialize the log level
	 */
	private static final EnumSerializer<LogLevel> LOG_LEVEL_SERIALIZER = new EnumSerializer<>(LogLevel.class);

	/**
	 * The timestamp when the log message was created.
	 */
	private Instant timestamp;

	/**
	 * The timestamp when the server received the log message.
	 */
	private Instant receivedAtServer;

	/**
	 * The content of the log message.
	 */
	private String message;

	/**
	 * The name of the client that sent the log message.
	 */
	private String clientName;

	/**
	 * The severity level of the log message.
	 */
	private LogLevel logLevel;
	

	/**
	 * Constructs a {@code LogMessage} with all details specified, including the server reception timestamp.
	 *
	 * @param serverReceivedAt The timestamp when the server received the message.
	 * @param occurredAt       The timestamp when the log message was created.
	 * @param clientName       The name of the client sending the log message.
	 * @param logLevel         The severity level of the log message.
	 * @param message          The content of the log message.
	 */
	public LogMessage(final Instant serverReceivedAt, final Instant occurredAt, final String clientName, final LogLevel logLevel, final String message) {
		this.timestamp = occurredAt;
		this.receivedAtServer = serverReceivedAt;
		this.message = message;
		this.clientName = clientName;
		this.logLevel = logLevel;
	}

	/**
	 * Constructs a {@code LogMessage} without specifying the server reception timestamp.
	 * The server reception timestamp can be set later using {@link #setReceivedAtServer(Instant)}.
	 *
	 * @param occurredAt  The timestamp when the log message was created.
	 * @param clientName The name of the client sending the log message.
	 * @param logLevel   The severity level of the log message.
	 * @param message    The content of the log message.
	 */
	public LogMessage(final Instant occurredAt, final String clientName, final LogLevel logLevel, final String message) {
		this(null, occurredAt, clientName, logLevel, message);
	}

	/**
	 * Retrieves the timestamp when the log message was created.
	 *
	 * @return The creation timestamp of the log message.
	 */
	public Instant getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Retrieves the content of the log message.
	 *
	 * @return The log message content.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Retrieves the name of the client that sent the log message.
	 *
	 * @return The client's name.
	 */
	public String getClientName() {
		return this.clientName;
	}

	/**
	 * Retrieves the severity level of the log message.
	 *
	 * @return The {@link LogLevel} of the log message.
	 */
	public LogLevel getLogLevel() {
		return this.logLevel;
	}

	/**
	 * Sets the timestamp when the server received the log message.
	 *
	 * @param receivedAt The server reception timestamp to set.
	 */
	public void setReceivedAtServer(final Instant receivedAt) {
		this.receivedAtServer = receivedAt;
	}

	/**
	 * Retrieves the timestamp when the server received the log message.
	 *
	 * @return The server reception timestamp, or {@code null} if not set.
	 */
	public Instant getReceivedAtServer() {
		return this.receivedAtServer;
	}

	/**
	 * Returns a string representation of the log message, including the timestamp, log level, and message content.
	 * The timestamp is formatted in UTC using the pattern {@code yyyy-MM-dd'T'HH:mm:ss}.
	 *
	 * @return A formatted string representing the log message.
	 */
	@Override
	public String toString() {
		LocalDateTime dateTime = LocalDateTime.ofInstant(this.getTimestamp(), ZoneId.of("UTC"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

		return dateTime.format(formatter) + " [" + this.logLevel.name() + "] " + this.message;
	}

	@Override
	public int hashCode() {
		return Objects.hash(timestamp, clientName, message, logLevel);
	}


	/**
	 * Compares this {@code LogMessage} to another object for equality.
	 * Two {@code LogMessage} instances are considered equal if they have the same timestamp,
	 * client name, message content, and log level.
	 *
	 * @param other The object to compare with.
	 * @return {@code true} if the objects are equal; {@code false} otherwise.
	 */
	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}

		if (!(other instanceof LogMessage logMessage)) {
			return false;
		}

		return logMessage.getTimestamp().toEpochMilli() == this.getTimestamp().toEpochMilli()
				&& logMessage.getClientName().equals(this.getClientName())
				&& logMessage.getMessage().equals(this.getMessage())
				&& logMessage.getLogLevel().equals(this.getLogLevel());
	}

	/**
	 * Serializes this {@code LogMessage} into a {@link NetworkMessageOutputStream}.
	 * The serialization format includes the serial version UID, log level, timestamps, client name, and message content.
	 *
	 * @param outputStream The output stream to write the serialized data to.
	 * @throws IOException If an I/O error occurs during serialization.
	 */
	@Override
	public void serialize(final NetworkMessageOutputStream outputStream) throws IOException {
		
		// Serial version
		outputStream.writeShort(serialVersionUID);

		// LogLevel
		LOG_LEVEL_SERIALIZER.write(outputStream, logLevel);

		// Timestamp
		outputStream.writeLong(this.timestamp.toEpochMilli());

		// ClientName
		byte[] clientNameBytes = this.clientName.getBytes(StandardCharsets.UTF_8);
		outputStream.writeInt(clientNameBytes.length);
		outputStream.write(clientNameBytes);

		// Message
		byte[] messageBytes = this.message.getBytes(StandardCharsets.UTF_8);
		outputStream.writeInt(messageBytes.length);
		outputStream.write(messageBytes);
	}

	/**
	 * Deserializes a {@code LogMessage} from a {@link NetworkMessageInputStream}.
	 * The deserialization process reads the log level, timestamp, client name, and message content from the input stream.
	 *
	 * @param inputStream The input stream to read the serialized data from.
	 * @return A new {@code LogMessage} instance constructed from the deserialized data.
	 * @throws IOException              If an I/O error occurs during deserialization.
	 * @throws IllegalArgumentException If the log level value is unknown or invalid.
	 */
	public static LogMessage deserialize(final NetworkMessageInputStream inputStream) throws IOException {

		// Get the log level
		LogLevel logLevel = LOG_LEVEL_SERIALIZER.read(inputStream);
		if (logLevel == null) {
			throw new IllegalArgumentException("Unknown log level");
		}

		// Read timestamp
		long timestampMs = inputStream.readLong();
		Instant timestamp = Instant.ofEpochMilli(timestampMs);

		// Read client name
		int clientNameLength = inputStream.readInt();
		byte[] clientNameBytes = new byte[clientNameLength];
		inputStream.readFully(clientNameBytes);
		String clientName = new String(clientNameBytes, StandardCharsets.UTF_8);

		// Read message
		int messageLength = inputStream.readInt();
		byte[] messageBytes = new byte[messageLength];
		inputStream.readFully(messageBytes);
		String message = new String(messageBytes, StandardCharsets.UTF_8);


		return new LogMessage(timestamp, clientName, logLevel, message);
	}
}
