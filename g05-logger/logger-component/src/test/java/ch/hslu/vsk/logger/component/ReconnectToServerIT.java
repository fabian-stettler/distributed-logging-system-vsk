package ch.hslu.vsk.logger.component;

import ch.hslu.vsk.logger.component.network.ReconnectConnection;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
class ReconnectToServerIT {

	private static int port = 50_000;

	private static int getMappedPort() {
		return loggerServer.getMappedPort(port);
	}

	private static String getHost() {
		return loggerServer.getHost();
	}
	private final Logger logger = Logger.getLogger(getClass().getName());


	@Container
	private static GenericContainer<?> loggerServer = new GenericContainer<>(DockerImageName.parse("g05/loggerserver:latest"))
														.withExposedPorts(port)
														.withStartupTimeout(Duration.ofSeconds(5))
														.waitingFor(Wait.forListeningPort());

	@Test
	 void testLoggerServerStart() {
		assertThat(loggerServer.getLogs()).contains("running").contains(Integer.toString(port));
	}

	@Test
	 void testWhileConnectedBefore() throws InterruptedException {

		// Create a new reconnect connection
		ReconnectConnection conn = new ReconnectConnection(getHost(), getMappedPort());
		conn.connect();

		// A new client should be connected now
		assertThat(loggerServer.getLogs()).contains("New client connected");

		// Stop the container
		loggerServer.stop();

		// Attempt to read from the connection to trigger a disconnect
		conn.nextObject();

		// Wait a bit
		Thread.sleep(200);

		// Start it again
		loggerServer.start();

		// Update the port on the connection because docker thinks its funny to dynamically assign ports
		conn.setPort(getMappedPort());

		long currentTime = System.currentTimeMillis();

		// We wait for 3s to give it some time
		while (System.currentTimeMillis() - currentTime < 3_000) {

			if (loggerServer.getLogs().contains("New client connected")) {
				// Happy
				return;
			}
		}

		assertTrue(false, "Client did not reconnect to the server. Container running: " + loggerServer.isRunning() + ". Logs: " + loggerServer.getLogs());
	}


	@Test
	 void testWhileNotConnectedBefore(){

		// Create a new reconnect connection
		ReconnectConnection conn = new ReconnectConnection(getHost(), getMappedPort());

		// Stop the container
		loggerServer.stop();

		// This should fail
		conn.connect();
		assertThat(conn.isConnected()).isFalse();

		// Start it again
		loggerServer.start();

		// Update the port on the connection because docker thinks its funny to dynamically assign ports
		conn.setPort(getMappedPort());

		long currentTime = System.currentTimeMillis();

		// We wait for 3s to give it some time
		while (System.currentTimeMillis() - currentTime < 3_000) {

			if (loggerServer.getLogs().contains("New client connected")) {
				// Happy
				return;
			}
		}


		assertTrue(false, "Client did not reconnect to the server. Container running: " + loggerServer.isRunning() + ". Logs: " + loggerServer.getLogs());
	}

}
