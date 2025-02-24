package ch.hslu.vsk.logger.server.viewer;

import ch.hslu.vsk.logger.common.LogMessage;
import jakarta.websocket.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class for the MessageDispatcherLoggerViewerServer. It takes the messages from the LoggerServer and sends them to the
 * connected  Viewer clients.
 */
public class MessageDispatcherLoggerViewerServer {

        private static final BlockingQueue<LogMessage> messageQueue = new LinkedBlockingQueue<>();
        //thread safe set to store the connected clients
        private static final Set<Session> clients = new CopyOnWriteArraySet<>();
        private static volatile boolean isRunning = false;
        private static final Logger LOGGER = LogManager.getLogger(MessageDispatcherLoggerViewerServer.class);


    /**
     * Add a new client to the list of connected clients.
     * Gets called from LogggerViewerServerEndpoint.
     * @param session
     */
    public static synchronized void addClient(Session session) {
        clients.add(session);
        LOGGER.info("MessageDispatcher added a new Client: {}" ,session.getId());
    }

    /**
     * Remove a client from the list of connected clients.
      * @param session
     */
    public static synchronized void removeClient(Session session) {
        clients.remove(session);
        LOGGER.info("Message Dispatcher removed a client!");
        if (clients.isEmpty()) {
            stopProcessing();
        }
    }

    /**
     * Add a new message to the queue.
     * Gets called from LoggerClient class.
     * @param logMessage
     */
    public static void queueMessage(LogMessage logMessage) {
        if (clients.isEmpty()){
            messageQueue.clear();
             return;
        }
        messageQueue.offer(logMessage);
        LOGGER.info("Message was offered by the LoggerServer");

    }

    /**
     * Start processing the messages in the queue.
     * Gets called from LoggerViewerServerEndpoint.
     * Starts a new thread that processes the messages in the queue and sends them to all connected clients.
     * One new thread for each LoggerViewerClient.
     */
    public static void startProcessing() {
        if (!isRunning) {
            isRunning = true;
            new Thread(() -> {
                while (isRunning) {
                    try {
                        LogMessage message = messageQueue.take();
                        sendToAllClients(message);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();
        }
    }

    /**
     * Stop processing the messages in the queue with setting isRunning to false.
     */
    public static void stopProcessing() {
        isRunning = false;
    }

    /**
     * Send a message to all connected clients with the open Sessions.
     * @param logMessage
     */
    private static void sendToAllClients(LogMessage logMessage) {
        if (clients.isEmpty()) {
            return;
        }
        for (Session client : clients) {
            if (client.isOpen()) {
                try {
                    client.getBasicRemote().sendText(logMessage.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
