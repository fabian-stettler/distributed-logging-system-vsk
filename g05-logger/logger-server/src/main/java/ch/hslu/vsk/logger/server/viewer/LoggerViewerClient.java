package ch.hslu.vsk.logger.server.viewer;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for the ViewerClient. Which can be started to view the logs on the server using WebSockets.
 */
@ClientEndpoint
public class LoggerViewerClient {

    private static final Logger logger = LogManager.getLogger(LoggerViewerClient.class);

    private final String serverAddress;
    private final int serverPort;
    private boolean isConnected = false;
    private Session session;
    private final LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public LoggerViewerClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    /**
     * Start the WebSocket client and connect to the server.
     */
    public void start() {

        try {
            URI endpointURI = new URI("ws://" + serverAddress + ":" + serverPort + "/loggerViewerServer");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, endpointURI);
            logger.info("Connected to LoggerViewerServer at " + endpointURI);
            isConnected = true;
            while (isConnected) {
                //logger.info("ViewerClient verarbeitet messages");
                String message = messageQueue.take();
                processMessage(message);
            }
            session.close();
        } catch(URISyntaxException | DeploymentException | IOException e) {
            logger.error("Error while connecting to WebSocket server", e);
        } catch (InterruptedException e) {
            logger.error("Error while processing messages", e);
        }


    }

    /**
     * Process a log message.
     * @param message is of type String but in Format of LogMessage (due to sending over WebSocket)
     */
    private void processMessage(String message) {
        try {
            logger.info(message);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid log message format", e);
        }
    }


    /**
     * Called when a new WebSocket message is received.
     */
    @OnMessage
    public void onMessage(String message) {
        messageQueue.offer(message);
    }

    /**
     * Called when the WebSocket connection is opened.
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        logger.info("Connection established: ");
    }

    /**
     * Called when the WebSocket connection is closed.
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
        isConnected = false;
        logger.info("Connection closed: " + reason.getReasonPhrase());
    }

    /**
     * Called when an error occurs.
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket error: ", throwable);
    }

    /**
     * Stop the WebSocket client.
     */
    public void stop() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            logger.error("Error while closing WebSocket connection", e);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public static void main(String[] args) {
        int serverPort;
        String serverAddress;

        //make it configurable
        if (args.length == 2) {
            serverAddress = args[0];
            serverPort = Integer.parseInt(args[1]);
        }
        else {
            serverAddress = "localhost";
            serverPort = 8025;
        }
        LoggerViewerClient client = new LoggerViewerClient(serverAddress, serverPort);
        client.start();
    }
}
