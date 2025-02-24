package ch.hslu.vsk.logger.server.network;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;

import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.adapter.LogAdapter;
import ch.hslu.vsk.logger.common.network.NetworkMessageInputStream;
import ch.hslu.vsk.logger.common.network.NetworkMessageOutputStream;
import ch.hslu.vsk.logger.common.network.NetworkTransferable;
import ch.hslu.vsk.logger.server.LoggerServer;
import ch.hslu.vsk.logger.server.viewer.MessageDispatcherLoggerViewerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * A client class representing a connected client in the LoggerServer.
 * This class handles client connection, message reception, sending messages, and managing incoming data streams.
 */
public final class LoggerClient implements Runnable {

    protected static final Logger logger = LogManager.getLogger(LoggerClient.class);

    private final LoggerServer server;
    private final Socket socket;
    private final LogAdapter logAdapter;
    private boolean connected;
    private NetworkMessageInputStream inputStream;
    private NetworkMessageOutputStream outputStream;

    /**
     * Constructor to create a new LoggerClient instance for a given socket connection.
     *
     * @param server The server to which this client belongs.
     * @param socket The socket representing the client connection.
     */
    public LoggerClient(LoggerServer server, Socket socket, LogAdapter adapter) {
        this.server = server;
        this.socket = socket;
        this.logAdapter = adapter;
        this.connected = true;
        try {
            this.inputStream = new NetworkMessageInputStream(this.socket.getInputStream());
            this.outputStream = new NetworkMessageOutputStream(this.socket.getOutputStream());
        } catch (IOException exception) {
            logger.error("Failed to initialize streams for client", exception);
            handleDisconnect();
        }
    }

	/**
	 * Checks whether the client is currently connected to the server.
	 *
	 * @return true if the client is connected; false otherwise.
	 */
	public boolean isConnected() {
		return this.connected;
	}

	/**
	 * Handles the disconnection of the client. Closes all resources and removes the client from the server.
	 */
	private void handleDisconnect() {
		this.connected = false;
		logger.info("Client {}:{} disconnected", this.socket.getInetAddress(), this.socket.getPort());
		server.removeClient(this);
		cleanupResources();
	}

	/**
	 * Cleans up the client's resources, such as the socket and input/output streams.
	 */
	private void cleanupResources() {
		try {
			if (this.inputStream != null) {
				this.inputStream.close();
			}
		} catch (IOException ex) {
			logger.error("Failed to close input stream", ex);
		}

		try {
			if (this.outputStream != null) {
				this.outputStream.close();
			}
		} catch (IOException ex) {
			logger.error("Failed to close output stream", ex);
		}

		try {
			if (this.socket != null && !this.socket.isClosed()) {
				this.socket.close();
			}
		} catch (IOException ex) {
			logger.error("Failed to close socket", ex);
		}
	}

	@Override
	public void run() {

		while (this.isConnected()) {

			// Read the next object
			NetworkTransferable parsed;
			try {
				parsed = this.inputStream.nextObject();  // Get the next message
			} catch (IOException ex) {
				logger.error("Error while receiving data from client", ex);
				handleDisconnect();
				break;
			}

			if (parsed instanceof LogMessage message) {
				message.setReceivedAtServer(Instant.now());
                this.logAdapter.saveLogMessage(message);
                MessageDispatcherLoggerViewerServer.queueMessage(message);
			}
		}
	}

	/**
	 * Sends a network transferable object to the client. Throws an exception if the client is not connected.
	 *
	 * @param object The object to send to the client.
	 * @throws IOException If the data could not be sent due to connection issues.
	 */
	public void send(final NetworkTransferable object) throws IOException {
		if (!this.isConnected() || this.socket == null) {
			throw new IllegalStateException("Tried to send data while client is not connected.");
		}

		try {
			this.outputStream.writeObject(object);  // Write the object to the output stream
		} catch (IOException ex) {
			logger.error("Error while sending data to client", ex);
			throw ex;
		}
	}

}
