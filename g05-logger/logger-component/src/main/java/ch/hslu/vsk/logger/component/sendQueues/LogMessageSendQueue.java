package ch.hslu.vsk.logger.component.sendQueues;

import ch.hslu.vsk.logger.common.LogMessage;


/**
 * Represents a queue that manages the enqueuing and sending of {@link LogMessage} objects to a server.
 */
public interface LogMessageSendQueue {

	/**
	 * Adds a {@link LogMessage} to the queue and initiates its transmission to the server.
	 *
	 * @param message The log message to be enqueued and sent.
	 */
	void enqueue(LogMessage message);
}

