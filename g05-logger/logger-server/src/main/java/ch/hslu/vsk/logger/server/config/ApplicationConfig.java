package ch.hslu.vsk.logger.server.config;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the configuration for the application.
 * Provides methods to initialize, modify, and retrieve configuration values such as
 * the network port, network interface, and log output file path.
 */
public class ApplicationConfig {


	/**
	 * Creates a new ApplicationConfig with default values.
	 */
	public ApplicationConfig() {
		// No instantiation needed
	}


	/**
	 * The network port used by the application. Defaults to {@code 50_000}.
	 */
	private int port = 50_000;

	/**
	 * The network interface for the application. Defaults to {@code null}.
	 */
	private NetworkInterface networkInterface = null;

	/**
	 * The file path for logging output. Defaults to {@code "./test.log"}.
	 */
	private Path logOutputFile = Path.of("./test.log");

	/**
	 * Returns the network port of the application.
	 *
	 * @return the port number as an integer.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Returns the network interface being used by the application.
	 *
	 * @return the {@link NetworkInterface} instance or {@code null} if not set.
	 */
	public NetworkInterface getNetworkInterface() {
		return this.networkInterface;
	}

	/**
	 * Returns the file path for the log output.
	 *
	 * @return the {@link Path} of the log output file.
	 */
	public Path getLogOutputFile() {
		return this.logOutputFile;
	}

	/**
	 * Creates an {@code ApplicationConfig} instance from the specified configuration source.
	 *
	 * @param source The {@link ConfigSource} to load the configuration values from.
	 * @return a new {@code ApplicationConfig} instance populated with values from the source.
	 */
	public static ApplicationConfig fromConfigSource(final ConfigSource source) {
		ApplicationConfig config = new ApplicationConfig();
		config.setConfigValuesFromSource(source);
		return config;
	}

	/**
	 * Merges the current configuration with another configuration source.
	 * Values from the provided {@link ConfigSource} will overwrite the existing ones.
	 *
	 * @param source The new {@link ConfigSource} to merge with the current configuration.
	 */
	public void mergeWith(final ConfigSource source) {
		this.setConfigValuesFromSource(source);
	}

	/**
	 * Sets all configuration values from the given {@link ConfigSource}.
	 * This method processes configuration keys such as {@code port}, {@code interface}, and {@code log_output_file}.
	 *
	 * @param source The source containing configuration values.
	 */
	private void setConfigValuesFromSource(final ConfigSource source) {

		// Set the port
		String portConfigValue = source.getConfigValue("listen_port");
		if (portConfigValue != null) {
			this.trySetPort(portConfigValue);
		}

		// Set the network interface
		String networkInterfaceConfigValue = source.getConfigValue("interface");
		if (networkInterfaceConfigValue != null) {
			this.trySetNetworkInterface(networkInterfaceConfigValue);
		}

		// Set the log output
		String logOutputFileConfigValue = source.getConfigValue("log_file");
		if (logOutputFileConfigValue != null) {
			this.trySetLogOutputFile(logOutputFileConfigValue);
		}
	}

	/**
	 * Tries to set the port from a string value, typically loaded from a configuration source.
	 * Validates that the value is a positive integer.
	 *
	 * @param portAsString The port value as a string.
	 * @throws IllegalArgumentException if the port is not a valid positive integer.
	 */
	private void trySetPort(final String portAsString) {
		int parsedPort;

		try {
			parsedPort = Integer.parseInt(portAsString);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("The port has to be an integer", ex);
		}

		if (parsedPort < 0) {
			throw new IllegalArgumentException("The port has to be a positive number");
		}

		this.port = parsedPort;
	}

	/**
	 * Tries to set the network interface from a string value, typically loaded from a configuration source.
	 * The string can either be an IP address or a network interface name.
	 *
	 * @param nInterface The network interface value as a string.
	 * @throws IllegalArgumentException if the value is not a valid IP address or interface name.
	 */
	private void trySetNetworkInterface(final String nInterface) {
		// Check for IP address
		Pattern ipAddressPattern = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
		Matcher matcher = ipAddressPattern.matcher(nInterface);
		if (matcher.matches()) {
			byte[] ipAddress = new byte[4];
			String[] parts = nInterface.split("\\.");
			try {
				for (int i = 0; i < ipAddress.length; i++) {
					ipAddress[i] = (byte) Integer.parseInt(parts[i]);
				}
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Invalid IP address format for network interface", ex);
			}

			try {
				this.networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByAddress(ipAddress));
			} catch (Exception ex) {
				throw new IllegalArgumentException("Unknown IP address", ex);
			}
		} else {
			// Treat as network interface name
			try {
				this.networkInterface = NetworkInterface.getByName(nInterface);
			} catch (SocketException ex) {
				throw new IllegalArgumentException("Invalid network interface", ex);
			}
		}
	}

	/**
	 * Tries to set the log output file path from a string value, typically loaded from a configuration source.
	 *
	 * @param file The file path as a string.
	 */
	private void trySetLogOutputFile(final String file) {
		this.logOutputFile = Path.of(file);
	}
}
