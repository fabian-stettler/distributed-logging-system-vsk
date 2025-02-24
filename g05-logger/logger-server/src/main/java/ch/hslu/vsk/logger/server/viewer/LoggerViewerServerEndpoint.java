package ch.hslu.vsk.logger.server.viewer;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.Session;

import java.time.Instant;

/**
 * Class for the ViewerServer. Handles connections to the ViewerClients via WebSockets and Sessions.
 */
@ServerEndpoint("/loggerViewerServer")
public class LoggerViewerServerEndpoint {

    private static final Logger logger = LogManager.getLogger(LoggerViewerServerEndpoint.class);

    /**
     * Called when a new client connects to the server.
     * Adds the client to the list of connected clients and starts processing messages.
     * @param session The session representing the client connection.
     */
    @OnOpen
    public void onOpen(Session session) {
        MessageDispatcherLoggerViewerServer.addClient(session);
        logger.info("Client added to the MessageDispatcher");
        MessageDispatcherLoggerViewerServer.queueMessage(new LogMessage(Instant.now(), "Connection", LogLevel.INFO, "Client connected: " + session.getId()));
        MessageDispatcherLoggerViewerServer.startProcessing();
    }

    /**
     * Called when a Session gets closed.
     * @param session The session representing the client connection.
     */
    @OnClose
    public void onClose(Session session) {
        MessageDispatcherLoggerViewerServer.removeClient(session);
    }

    /**
     * Called when there is an error with the LoggerViewerClient Session
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("Error with client " + session.getId(), throwable);
    }

}
