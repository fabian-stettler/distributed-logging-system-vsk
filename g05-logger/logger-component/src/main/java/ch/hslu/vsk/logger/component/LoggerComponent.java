package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.Logger;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.component.sendQueues.LogMessageSendQueue;

import java.time.Instant;

import org.apache.logging.log4j.LogManager;


/**
 * This class implements the {@link Logger} interface and provides functionality to log messages 
 * at various log levels. It uses a {@link LogMessageSendQueue} to queue log messages for further processing.
 * The log messages are associated with a specific client and can optionally respect a minimum log level.
 */
public final class LoggerComponent implements Logger {

	/**
	 * Logger for debugging this component.
	 */
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(LoggerComponent.class);

	private LogLevel minLogLevel;
	private String clientName;
	private LogMessageSendQueue sendQueue;


	/**
	 * Constructs a new {@code LoggerComponent} without a minimum log level.
	 * All log levels will be accepted and logged.
	 *
	 * @param sendQueue  The queue where log messages will be sent.
	 * @param clientName The name of the client associated with this logger.
	 */
	public LoggerComponent(final LogMessageSendQueue sendQueue, final String clientName) {
		this(sendQueue, clientName, null);
	}

	/**
	 * Constructs a new {@code LoggerComponent} with a specified minimum log level.
	 * Only messages at or above the minimum log level will be logged.
	 *
	 * @param sendQueue  The queue where log messages will be sent.
	 * @param clientName The name of the client associated with this logger.
	 * @param minLogLevel The minimum log level required for messages to be logged. 
	 *                    Messages with a lower log level will be ignored.
	 */
	public LoggerComponent(final LogMessageSendQueue sendQueue, final String clientName, final LogLevel minLogLevel) {
		this.sendQueue = sendQueue;
		this.clientName = clientName;
		this.minLogLevel = minLogLevel;
	}


	@Override
	public void log(final LogLevel logLevel, final String message) {

		if (this.minLogLevel != null && logLevel.getValue() > this.minLogLevel.getValue()) {
			LOGGER.info("Tried to log a message with a lower log level then the minLogLevel");
			return;
		}

		// Create a log message
		LogMessage logMessage = new LogMessage(Instant.now(), this.clientName, logLevel, message);

		// Queue the log message
		this.sendQueue.enqueue(logMessage);
	}

	@Override
	public void error(final String message) {
		this.log(LogLevel.ERROR, message);
	}

	@Override
	public void warning(final String message) {
		this.log(LogLevel.WARNING, message);
	}

	@Override
	public void info(final String message) {
		this.log(LogLevel.INFO, message);
	}

	@Override
	public void debug(final String message) {
		this.log(LogLevel.DEBUG, message);
	}
}
