package ch.hslu.vsk.logger.server;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.hslu.vsk.logger.common.util.FileCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.hslu.vsk.logger.common.adapter.LogAdapter;
import ch.hslu.vsk.logger.common.adapter.StringPersistorLogAdapter;
import ch.hslu.vsk.logger.common.formatter.LogMessageFormat;
import ch.hslu.vsk.logger.common.formatter.LogMessageFormatterFactory;
import ch.hslu.vsk.logger.server.network.LoggerClient;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;
import ch.hslu.vsk.stringpersistor.impl.StringPersistorFile;
import org.glassfish.tyrus.server.Server;
import ch.hslu.vsk.logger.server.viewer.LoggerViewerServerEndpoint;
/**
 * A server that handles client connections for logging messages and persists them using different log message formats.
 * It manages a pool of connected clients and provides functionality to start the server, handle incoming connections,
 * process log messages, and respond to clients.
 */
public class LoggerServer {

	/**
	 * Logger for debugging.
	 */
	protected static final Logger LOGGER = LogManager.getLogger(LoggerServer.class);

	private NetworkInterface networkInterface;
	private LogAdapter logAdapter;
	private int port;
	private ServerSocket serverSocket;
	private boolean isRunning = false;
	private int maxConnections = 50;
	private ExecutorService clientHandlerPool;
	private List<LoggerClient> clients = new ArrayList<>();
	private Server loggerViewerServer;
	private static final String HOST_NAME_LOGGER_VIEWER_SERVER = "localhost";
	private static final int PORT_LOGGER_VIEWER_SERVER = 8025;


	/**
	 * Constructor to create a new LoggerServer with a specified port and log file path.
	 * This server uses a default SpeedFormat log message formatter.
	 *
	 * @param networkInterface The network interface to bind to.
	 * @param port The port number to listen for connections.
	 * @param logFile The path to the log file for persisting log messages.
	 */
	public LoggerServer(final NetworkInterface networkInterface, final int port, final Path logFile) {
		this(networkInterface, port, logFile, LogMessageFormat.COMPETITION);  // Default format
	}

	/**
	 * Constructor to create a new LoggerServer with a specified port, log file, and log format type.
	 *
	 * @param networkInterface The network interface to bind to.
	 * @param port The port number to listen for connections.
	 * @param logFile The path to the log file for persisting log messages.
	 * @param format The type of log format to use (e.g., JSON, HumanReadable, SpeedFormat).
	 */
	public LoggerServer(final NetworkInterface networkInterface, final int port, final Path logFile, final LogMessageFormat format) {
		this.networkInterface = networkInterface;
		this.port = port;
		StringPersistor stringPersistor = new StringPersistorFile();
		if (!FileCreator.createFile(logFile)) {
			throw new IllegalArgumentException("Invalid log file path. Failed to create the file");
		}
		stringPersistor.setFile(logFile);
		this.logAdapter = new StringPersistorLogAdapter(stringPersistor, LogMessageFormatterFactory.createFormatter(format));
		this.loggerViewerServer = new Server(HOST_NAME_LOGGER_VIEWER_SERVER, PORT_LOGGER_VIEWER_SERVER, "/", null, LoggerViewerServerEndpoint.class);
	}

	/**
	 * Starts the LoggerServer, which binds the server socket to the specified port
	 * and begins accepting client connections.
	 */
	public void start() {
		try {
			// Start the socket
			if (this.networkInterface != null) {
				this.serverSocket = new ServerSocket(this.port, this.maxConnections, this.networkInterface.getInetAddresses().nextElement());
			} else {
				this.serverSocket = new ServerSocket(this.port, this.maxConnections);
			}
		} catch (IOException e) {
			LOGGER.error("Failed to start server socket", e);
			return;
		}

		// LoggerViewerServer starten
		try {
			LOGGER.info("Starting LoggerViewerServer...");
			this.loggerViewerServer.start(); // Startet den LoggerViewerServer
			LOGGER.info("LoggerViewerServer started at ws://{}:{}",HOST_NAME_LOGGER_VIEWER_SERVER, this.loggerViewerServer.getPort());
		} catch (Exception e) {
			LOGGER.error("Failed to start LoggerViewerServer", e);
		}


		this.isRunning = true;
		LOGGER.info("LoggerServer is running on tcp://{}:{} [MaxClients={}]",
				this.serverSocket.getInetAddress().getHostAddress(),
				this.port,
				this.maxConnections);

		// Create an executor service for managing client connections
		this.clientHandlerPool = Executors.newVirtualThreadPerTaskExecutor();
		this.handleIncomingConnections();
	}

	/**
	 * Listens for incoming client connections, and handles each client connection in a separate thread
	 * using the clientHandlerPool.
	 */
	private void handleIncomingConnections() {
		while (this.isRunning) {
			try {
				// Accept a new client connection
				Socket clientSocket = this.serverSocket.accept();
				LOGGER.info("New client connected: {}:{}", clientSocket.getInetAddress(), clientSocket.getPort());

				// Handle the client in a separate thread using the executor pool
				LoggerClient client = new LoggerClient(this, clientSocket, this.logAdapter);
				this.clients.add(client);

				this.clientHandlerPool.submit(client);
			} catch (IOException exception) {
				LOGGER.error("Failed to accept client", exception);
			}
		}
	}


	/**
	 * Removes a client from the connected client list.
	 *
	 * @param client The client to remove.
	 */
	public void removeClient(final LoggerClient client) {
		this.clients.remove(client);
	}

	/**
	 * Stops the server, closes the server socket, and shuts down the client handler pool.
	 */
	public void stop() {
		this.isRunning = false;
		try {
			this.serverSocket.close();
			this.clientHandlerPool.shutdownNow();
		} catch (IOException e) {
			LOGGER.error("Failed to stop server", e);
		}
	}

	/**
	 * Returns the list of currently connected clients.
	 *
	 * @return The list of {@link LoggerClient} objects.
	 */
	public List<LoggerClient> getClients() {
		return new ArrayList<>(clients);
	}

	/**
	 * Returns the port number the server is running on.
	 *
	 * @return The port number.
	 */
	public int getPort() {
		return this.port;
	}
}
