package ch.hslu.vsk.logger.server.viewer;

import ch.hslu.vsk.logger.common.*;
import ch.hslu.vsk.logger.common.formatter.*;
import ch.hslu.vsk.logger.common.network.NetworkTransferable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class for the ViewerServer. Handles connections to the ViewerClients
 */
public class LoggerViewerServer {

    private int port;
    private boolean isRunning = false;
    private ServerSocket serverSocket;
    private List<Socket> clients = new ArrayList<>();
    private int maxConnections;
    protected static final Logger logger = LogManager.getLogger(LoggerViewerServer.class);
    private LogMessageFormatter formatter;
    private BlockingQueue<NetworkTransferable> messageQueue = new LinkedBlockingQueue<>();


    /**
     * Create a new LoggerViewerServer instance for a certain port
     */
    public LoggerViewerServer(int port, int maxConnections, LogMessageFormat displayType) {
        if (port > 65535 || port < 0){
            logger.error("Port is invalid, LoggerViewerServer can not be instantiated on this port. Choose port between 0 and 65535");
            return;
        }
        this.port = port;
        this.maxConnections = maxConnections;
        switch (displayType) {
            case JSON -> formatter = new JSONLogMessageFormatter();
            case HUMAN_READABLE -> formatter = new HumanReadableLogMessageFormatter();
            case SPEED_FORMAT -> formatter = new SpeedFormatter();
            case XML -> formatter = new XMLLogMessageFormatter();
            default -> throw new IllegalArgumentException("Unsupported log type.");
        };
    }

    public LoggerViewerServer(int port, int maxConnections) {
        this(port, maxConnections, LogMessageFormat.SPEED_FORMAT);
    }

    /**
     * Start the server
     */
    public void start(){
        try {
            serverSocket = new ServerSocket(port, maxConnections);
            new Thread(this::processMessages).start(); // Start message processing thread
        } catch (IOException e) {
            throw new RuntimeException("Server Socket failed to start.");
        }
        this.isRunning = true;
        this.handlingConnections();
    }

    private void handlingConnections() {
        try{
            while(isRunning){
                Socket loggerViewerClient = serverSocket.accept();
                //add client
                synchronized (clients) {
                    clients.add(loggerViewerClient);
                }
                logger.info("Added new LoggerViewerClient");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Send a NetworkTransferable to the ViewerServer
     * @param transferable the NetworkTransferable to send
     */
    public void sendNetworkTransferableToViewerClients(NetworkTransferable transferable) {
        String message = formatter.serialize((LogMessage) transferable);
            for (Socket viewer : clients) {
                try {
                    viewer.getOutputStream().write((message + "\n").getBytes());
                    viewer.getOutputStream().flush();
                } catch (IOException e) {
                    logger.error("Failed to send log message to viewer", e);
                }
            }
    }

    /**
     * Process messages from the message queue and send them to the viewer clients
     */
    private void processMessages() {
        while (isRunning) {
            try {
                NetworkTransferable message = messageQueue.take();
                synchronized (clients) {
                    sendNetworkTransferableToViewerClients(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Message processing thread interrupted", e);
            }
        }
    }

    public void queueMessage(NetworkTransferable message) {
        messageQueue.offer(message);
    }

    public void stop() {
        this.isRunning = false;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            logger.error("Failed to stop LoggerViewerServer", e);
        }
    }
}