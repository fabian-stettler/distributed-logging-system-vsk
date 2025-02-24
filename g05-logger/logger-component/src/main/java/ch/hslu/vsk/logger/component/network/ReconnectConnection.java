package ch.hslu.vsk.logger.component.network;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A specialized version of {@link Connection} that automatically attempts to reconnect to the server
 * after a disconnection event. The reconnection attempts continue until a successful connection is established.
 */
public class ReconnectConnection extends Connection {

	private static final Logger LOGGER = LogManager.getLogger(ReconnectConnection.class);

    /**
     * Default interval (in milliseconds) between reconnection attempts.
     */
    private static final int DEFAULT_RECONNECT_INTERVAL_MS = 1_000;

    private final ScheduledExecutorService scheduler;
	private boolean isReconnecting = false;
	private ScheduledFuture<?> reconnectFuture = null;
    private int reconnectIntervalMs;

    /**
     * Creates a new {@code ReconnectConnection} with the default reconnect interval.
     *
     * @param host The server's hostname or IP address.
     * @param port The port number to connect to.
     */
    public ReconnectConnection(final String host, final int port) {
        this(host, port, DEFAULT_RECONNECT_INTERVAL_MS);
    }

    /**
     * Creates a new {@code ReconnectConnection} with a specified reconnect interval.
     *
     * @param host                The server's hostname or IP address.
     * @param port                The port number to connect to.
     * @param reconnectIntervalMs The interval (in milliseconds) between reconnection attempts.
     */
    public ReconnectConnection(final String host, final int port, final int reconnectIntervalMs) {
        super(host, port);
        this.reconnectIntervalMs = reconnectIntervalMs;
		this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Handles the disconnection event by initiating an automatic reconnection loop.
     * This loop attempts to reconnect at the specified interval until a successful connection is established.
     */
    @Override
    protected void handleDisconnect() {
        super.handleDisconnect();

        // Avoid starting multiple reconnection threads
        if (this.isReconnecting) {
            return;
        }

		this.isReconnecting = true;
        LOGGER.info("Attempting to reconnect to server using interval of {} ms", this.reconnectIntervalMs);

		this.reconnectFuture = this.scheduler.scheduleAtFixedRate(() -> {

            // Attempt to reconnect
            if (this.tryConnect()) {
				this.isReconnecting = false;
				this.reconnectFuture.cancel(false);
                this.notifyConnectListeners();
            }
		}, 0, this.reconnectIntervalMs, TimeUnit.MILLISECONDS);
	}
}
