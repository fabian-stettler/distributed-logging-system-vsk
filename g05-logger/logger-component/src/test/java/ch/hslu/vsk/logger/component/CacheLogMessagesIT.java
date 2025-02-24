package ch.hslu.vsk.logger.component;


import ch.hslu.vsk.logger.api.Logger;
import ch.hslu.vsk.logger.component.network.Connection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Testcontainers
final class CacheLogMessagesIT {

	private List<GenericContainer<?>> createdContainers = new ArrayList<>();
	private String logFile = "/testing/server_logs/server_logs.log";

	private GenericContainer<?> createServerContainer(final int port) {
		var container = new GenericContainer<>(DockerImageName.parse("g05/loggerserver:latest"))
				.withCommand("--listen-port=" + port + " --log-file=" + logFile)
				.withExposedPorts(port)
				.withStartupTimeout(Duration.ofSeconds(5))
				.waitingFor(Wait.forListeningPort())
				.withFileSystemBind("./tmp", "/testing", BindMode.READ_WRITE);

		container.setPortBindings(Arrays.asList(port + ":" + port));

		this.createdContainers.add(container);

		return container;
	}

	@BeforeEach
	@AfterEach
    void cleanup() throws  IOException {
        createdContainers.forEach(GenericContainer::close);
        createdContainers.clear();

		Path tmpPath = Path.of("./tmp");

		if (!Files.exists(tmpPath)) {
			return;
		}

		// Walk the directory tree and delete each file
		Files.walkFileTree(tmpPath, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				// Delete individual files
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
				// After visiting the directory, delete the directory itself
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});

		System.out.println("Directory and its contents deleted successfully.");
	}

    @Test
    void testConnectToServer() {
        int port = 55_554;
        var server = createServerContainer(port);
        server.start();

		try (Connection conn = new Connection(server.getHost(), port)) {
			conn.connect();

			assertTrue(conn.isConnected(), "Connection should be established.");

			server.stop();
			server.start();
			conn.connect();

			assertTrue(conn.isConnected(), "Connection should be re-established after restart.");
		} catch (IOException ex) {
			System.out.println("Failed to close connection");
		}
	}

	@ParameterizedTest
	@CsvSource({ "10", "100", "1_000", "10_000" })
	void testWithoutConnectionInterrupt(final int amount) throws InterruptedException {
		int port = 55_550;
		var server = createServerContainer(port);
		server.start();

		Logger logger = (new LoggerFactory()).getLogger(server.getHost() + ":" + port, "Client1");

		// Log the messages
		for (int i = 1; i <= amount; i++) {
			logger.debug("Message number: " + i);
		}

		Map<Integer, Integer> receivedLogs = fetchLogsFromServer(server, amount, Duration.ofSeconds(5), true);

		// Validate the logs are in order and differ by one
		assertTrue(areValuesInOrderAndOneApart(receivedLogs), "Values should be in order and one apart.");
	}


	/**
	 * Checks if the values of the map are in order and each one apart.
	 *
	 * @param map The map to check
	 * @return {@code true} if all values are in order and one apart, otherwise {@code false}
	 */
	public boolean areValuesInOrderAndOneApart(final Map<Integer, Integer> map) {
		Integer previous = null;
		for (Integer current : map.values()) {
			if (previous != null && current != previous + 1) {
				return false;
			}
			previous = current;
		}
		return true;
	}

	/**
	 * Fetches logs from the server container with a timeout counting from the last received log entry.
	 * @param server The server container.
	 * @param expectedAmount The expected number of log entries.
	 * @param timeout The maximum duration to wait for logs after the last received log entry.
	 * @param failAfterTimeout Should the test fail after the given timeout or return the received Logs
	 * @return A map of log line numbers to log message numbers.
	 */
	private Map<Integer, Integer> fetchLogsFromServer(final GenericContainer<?> server, final int expectedAmount, final Duration timeout, final boolean failAfterTimeout) throws InterruptedException {
		Map<Integer, Integer> receivedLogs = new HashMap<>();
		String commandBase = "tail -n +";  // Tail command with starting line number
		Pattern pattern = Pattern.compile("Message number: (\\d+)");

		int lastReadLine = 0;  // Track the last line number we processed
		long lastReceivedTime = System.currentTimeMillis(); // Time of the last log entry

		// Read logs in a loop until we get the expected amount
		while (receivedLogs.size() < expectedAmount) {
			try {
				// If the time since the last log read exceeds the timeout, handle it
				if (System.currentTimeMillis() - lastReceivedTime > timeout.toMillis()) {

					if (failAfterTimeout) {
						Assertions.fail("Timeout exceeded while fetching logs. No new logs received for " + timeout.toSeconds() + " seconds.");
						break;
					}

					break;
				}

				// Use tail to read only new lines, starting from the last processed line
				String command = commandBase + (lastReadLine + 1) + " " + logFile;  // Start reading from the next unprocessed line
				String result = server.execInContainer("sh", "-c", command).getStdout();
				boolean newLogsReceived = parseLogLines(result, pattern, receivedLogs, lastReadLine);

				// If new logs are received, update the last received time and line number
				if (newLogsReceived) {
					lastReceivedTime = System.currentTimeMillis(); // Reset the last received time when new logs are found
					lastReadLine = receivedLogs.size();  // Update the last read line number to match the current size
				}

				Thread.sleep(50);  // Wait before retrying

			} catch (IOException | InterruptedException ex) {
				Assertions.fail("Error reading logs from container", ex);
			}
		}

		return receivedLogs;
	}

	/**
	 * Parses the log lines and adds matched numbers to the map.
	 * @param logContent The log content to parse.
	 * @param pattern The pattern to extract message numbers.
	 * @param receivedLogs The map to store parsed log entries.
	 * @param lastReadLine The last line number that was read.
	 * @return true if new logs were received, false otherwise.
	 */
	private boolean parseLogLines(final String logContent, final Pattern pattern, final Map<Integer, Integer> receivedLogs, final int lastReadLine) {
		int lineNumber = lastReadLine + 1;  // Start from the next line number
		boolean newLogsReceived = false;

		for (String line : logContent.split("\n")) {
			Matcher matcher = pattern.matcher(line);

			if (matcher.find() && lineNumber > lastReadLine) {
				// Only add new log entries
				receivedLogs.put(lineNumber, Integer.parseInt(matcher.group(1)));
				newLogsReceived = true;
			}
			lineNumber++;
		}


		return newLogsReceived;
	}
}
