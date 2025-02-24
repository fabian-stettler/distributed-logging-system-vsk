package ch.hslu.vsk.logger.component.sendQueues;

import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.component.cache.LogMessageCacheStrategy;
import ch.hslu.vsk.logger.component.network.ReconnectConnection;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * A class that handles queuing and sending {@link LogMessage} objects while ensuring that messages are sent
 * in the order they were enqueued, with any cached messages being sent first upon reconnection.
 * <p>
 * This class uses a {@link LogMessageCacheStrategy} to cache log messages when the system is disconnected
 * and sends cached messages upon reconnection before any new messages are transmitted.
 * </p>
 * The class manages a connection through a {@link ReconnectConnection} object and handles reconnection logic
 * through a listener. The message order is preserved using a {@link BlockingQueue}.
 */
public final class BlockingLogMessageSendQueue implements LogMessageSendQueue {

    private final ReconnectConnection connection;
    private final LogMessageCacheStrategy cacher;
	private boolean connectionReady = false;
	private final Object sendLock = new Object();

    /**
     * Constructs a {@code BlockingLogMessageSendQueue} that manages sending and caching log messages.
     * It establishes a connection to the specified host and port, and sets up a listener to handle reconnections.
     *
     * @param cacher the {@link LogMessageCacheStrategy} to be used for caching messages when disconnected; must not be null.
     * @param host the host to which the connection will be made; must not be null.
     * @param port the port number for the connection.
     */
    public BlockingLogMessageSendQueue(final LogMessageCacheStrategy cacher, final String host, final int port) {
        if (cacher == null || host == null) {
            throw new IllegalArgumentException("Cacher and host must not be null.");
        }

        this.cacher = cacher;
        this.connection = new ReconnectConnection(host, port, 50);
        this.connection.addConnectListener(this::handleReconnect);
        this.connection.connect();
    }

    /**
     * Handles the reconnection process. This method is invoked when the connection is re-established.
     * It ensures that all cached messages are sent before any new messages are transmitted.
     * This method is executed in a separate thread, so synchronization is necessary.
     */
    private void handleReconnect() {

		synchronized (this.sendLock) {

        	// Send cached messages first
        	List<LogMessage> cachedMessages = this.cacher.retrieveAll();
			this.cacher.clear();
        	for (LogMessage message : cachedMessages) {

            	// Try to send the cached messages before processing any new ones
            	if (!this.connection.send(message)) {

                	// If sending fails, keep the message cached
                	this.cacher.save(message);
            	}
        	}

			this.connectionReady = this.connection.isConnected();
		}
    }

    /**
     * Enqueues a {@link LogMessage} to be sent.
     * If the system is disconnected, the message is cached using the {@link LogMessageCacheStrategy}.
     * If connected, the message is sent immediately. If sending the message fails, it is cached for later.
     *
     * @param message the {@link LogMessage} to enqueue and send; must not be null.
     */
    @Override
    public void enqueue(final LogMessage message) {

        if (message == null) {
            throw new IllegalArgumentException("LogMessage must not be null.");
        }

		synchronized (this.sendLock) {

			// If the connection is not ready yet -> cache it
			if (!this.connectionReady) {
				this.cacher.save(message);
				return;
			}

        	// Try to send the log message
        	if (!this.connection.send(message)) {

            	// Failed to send -> cache the message
            	this.cacher.save(message);
				this.connectionReady = false;
        	}

        	// Successfully sent, continue :)
		}

    }
}
