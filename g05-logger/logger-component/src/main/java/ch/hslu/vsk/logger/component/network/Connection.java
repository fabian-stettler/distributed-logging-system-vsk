package ch.hslu.vsk.logger.component.network;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.hslu.vsk.logger.common.network.NetworkMessageInputStream;
import ch.hslu.vsk.logger.common.network.NetworkMessageOutputStream;
import ch.hslu.vsk.logger.common.network.NetworkTransferable;

/**
 * This class facilitates establishing a connection to a server, enabling data transmission and reception.
 * It supports adding listeners for connect and disconnect events and provides methods for sending and
 * receiving {@link NetworkTransferable} objects.
 */
public class Connection implements AutoCloseable {

    /**
     * Logger used for debugging and logging connection events.
     */
    private static final Logger LOGGER = LogManager.getLogger(Connection.class);

    private Socket socket;
    private String host;
    private int port;
    private NetworkMessageOutputStream outputStream;
    private NetworkMessageInputStream inputStream;
    private boolean connected = false;

    private final List<ConnectListener> connectListeners = new ArrayList<>();
    private final List<DisconnectListener> disconnectListeners = new ArrayList<>();

    /**
     * A listener interface to handle connection establishment events.
     */
    public interface ConnectListener {

        /**
         * Invoked when the connection to the server is successfully established.
         */
        void onConnect();
    }

    /**
     * A listener interface to handle connection termination events.
     */
    public interface DisconnectListener {

        /**
         * Invoked when the connection to the server is terminated.
         */
        void onDisconnect();
    }

    /**
     * Registers a listener to be notified when the connection is established.
     *
     * @param listener The listener to be added.
     */
    public void addConnectListener(final ConnectListener listener) {
        this.connectListeners.add(listener);
    }

    /**
     * Removes a listener from the connect notifications.
     *
     * @param listener The listener to be removed.
     */
    public void removeConnectListener(final ConnectListener listener) {
        this.connectListeners.remove(listener);
    }

    /**
     * Registers a listener to be notified when the connection is disconnected.
     *
     * @param listener The listener to be added.
     */
    public void addDisconnectListener(final DisconnectListener listener) {
        this.disconnectListeners.add(listener);
    }

    /**
     * Removes a listener from the disconnect notifications.
     *
     * @param listener The listener to be removed.
     */
    public void removeDisconnectListener(final DisconnectListener listener) {
        this.disconnectListeners.remove(listener);
    }

    /**
     * Constructs a new {@code Connection} to the specified host and port.
     *
     * @param host The server's hostname or IP address.
     * @param port The port number on the server.
     */
    public Connection(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Sets the host for the connection. Changing the host does not trigger a reconnect.
     *
     * @param host The new host.
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * Retrieves the current host for the connection.
     *
     * @return The hostname or IP address.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Sets the port for the connection. Changing the port does not trigger a reconnect.
     *
     * @param port The new port.
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * Retrieves the current port for the connection.
     *
     * @return The port number.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Attempts to reconnect to the server by establishing a new connection.
     *
     * @return {@code true} if the connection was successfully re-established, otherwise {@code false}.
     */
    protected boolean tryConnect() {
        try {
            this.socket = new Socket(this.host, this.port);
            this.outputStream = new NetworkMessageOutputStream(this.socket.getOutputStream());
            this.inputStream = new NetworkMessageInputStream(this.socket.getInputStream());
            this.connected = true;
			LOGGER.info("Successfully connected to tcp://{}:{}", host, port);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to connect to server tcp://{}:{}", host, port);
            this.handleDisconnect();
            return false;
        }
    }

    /**
     * Notifies all registered connect listeners that the connection was established.
     */
    protected void notifyConnectListeners() {
    	for (ConnectListener listener : this.connectListeners) {
 			listener.onConnect();
    	}
    }

    /**
     * Notifies all registered disconnect listeners that the connection was terminated.
     */
    protected void notifyDisconnectListeners() {
        for (DisconnectListener listener : this.disconnectListeners) {
            listener.onDisconnect();
        }
    }

    /**
     * Initiates the connection to the server. If the connection attempt is successful,
     * registered listeners will be notified.
     */
    public void connect() {
        if (this.tryConnect()) {
            this.notifyConnectListeners();
        }
    }

    /**
     * Handles a disconnection event, releasing resources and notifying listeners.
     */
    protected void handleDisconnect() {
        this.connected = false;

		LOGGER.info("Disconnecting from tcp://{}:{}", host, port);

        if (this.outputStream != null) {
            try {
                this.outputStream.close();
				this.outputStream = null;
            } catch (IOException ex) {
                LOGGER.info("Failed to close output stream", ex);
            }
        }

        if (this.inputStream != null) {
            try {
                this.inputStream.close();
				this.inputStream = null;
            } catch (IOException ex) {
                LOGGER.info("Failed to close input stream", ex);
            }
        }

        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                LOGGER.info("Failed to close socket to tcp://{}:{}", host, port);
            }
        }
    }

    /**
     * Checks if the connection to the server is active.
     *
     * @return {@code true} if connected, otherwise {@code false}.
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Reads the next {@link NetworkTransferable} object from the input stream. This method blocks
     * until a message is received or an error occurs.
     *
     * @return The received {@link NetworkTransferable} object, or {@code null} if an error occurs.
     */
    public NetworkTransferable nextObject() {
        try {
            return this.inputStream.nextObject();
        } catch (IOException ex) {
            this.handleDisconnect();
            this.notifyDisconnectListeners();
            return null;
        }
    }

    /**
     * Sends a {@link NetworkTransferable} object to the server.
     *
     * @param data The object to send.
     * @return {@code true} if the object was successfully sent, otherwise {@code false}.
     */
    public boolean send(final NetworkTransferable data) {
        if (!this.isConnected()) {
            LOGGER.debug("Attempted to send data while not connected.");
            return false;
        }

        try {
            this.outputStream.writeObject(data);
            return true;
        } catch (IOException ex) {
            LOGGER.debug("Error sending data to tcp://{}:{}. Data: {}", host, port, data, ex);
            this.handleDisconnect();
            this.notifyDisconnectListeners();
            return false;
        }
    }

    /**
     * Closes the connection, releasing all resources.
     *
     * @throws IOException If an error occurs while closing the connection.
     */
    @Override
    public void close() throws IOException {
        this.handleDisconnect();
    }
}
