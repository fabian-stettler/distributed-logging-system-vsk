package ch.hslu.vsk.logger.server;


import ch.hslu.vsk.logger.server.config.ApplicationConfig;
import ch.hslu.vsk.logger.server.config.ArgsConfigSource;
import ch.hslu.vsk.logger.server.config.EnvConfigSource;


/**
 * The {@code Application} class serves as the entry point for the application.
 * It initializes configuration settings, sets up a server instance, and starts
 * the server to handle logging operations.
 */
public final class Application {



	/**
	 * Class should not be instantiated.
	 */
	private Application() { }


	/**
	 * The main method that acts as the entry point for the application. It performs
	 * the following steps:
	 * <ul>
	 *   <li>Loads configuration from the environment variables (via a `.env` file).</li>
	 *   <li>Merges configuration from command-line arguments.</li>
	 *   <li>Creates and configures a {@link LoggerServer} instance using the provided settings.</li>
	 *   <li>Starts the {@code LoggerServer} to listen on the specified network interface and port.</li>
	 * </ul>
	 *
	 * @param args Command-line arguments to override or extend configuration.
	 */
	public static void main(final String[] args){
		
		// Get the config from .env file
		ApplicationConfig config = ApplicationConfig.fromConfigSource(new EnvConfigSource());

		// Merge with args
		config.mergeWith(new ArgsConfigSource(args));

		// Create server instance
		LoggerServer server = new LoggerServer(
			config.getNetworkInterface(),
			config.getPort(),
			config.getLogOutputFile()
		);

		// Start the server
		server.start();
	}
}

