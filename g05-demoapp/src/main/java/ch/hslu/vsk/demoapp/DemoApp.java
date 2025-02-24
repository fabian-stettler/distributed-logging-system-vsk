/*
 * Copyright 2024 Roland Gisler, HSLU Informatik, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.hslu.vsk.demoapp;

import java.util.Scanner;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.Logger;
import ch.hslu.vsk.logger.api.LoggerSetupFactory;

/**
 * Demo-Application for showcasing the functionality of the logger system.
 */
public final class DemoApp {

	private static String loggerImplementation;
	private static String serverHost;
	private static String serverPort;
	private static String clientName;
	private static String loggerUrl;

	/**
	 * Configures the logger based on user input.
	 *
	 * @param scanner A {@link Scanner} to read user input.
	 * @return A configured {@link Logger} instance.
	 */
	private static Logger configureLogger(final Scanner scanner){
		System.out.println("Logger Configuration");
		System.out.println("--------------------");

		System.out.println("Logger implementation (ch.hslu.vsk.logger.component.LoggerFactory): ");
		loggerImplementation = scanner.nextLine().trim();
		if (loggerImplementation.isEmpty()){
			loggerImplementation = "ch.hslu.vsk.logger.component.LoggerFactory";
		}

		System.out.println("Server host (localhost): ");
		serverHost = scanner.nextLine().trim();
		if (serverHost.isEmpty()){
			serverHost = "localhost";
		}

		System.out.println("Server port (50'000): ");
		serverPort = scanner.nextLine().trim();
		if (serverPort.isEmpty()){
			serverPort = "50000";
		}

		System.out.println("Client name (DemoApp): ");
		clientName = scanner.nextLine().trim();
		if (clientName.isEmpty()){
			clientName = "DemoApp";
		}

		loggerUrl = "tcp://" + serverHost + ":" + serverPort;
		return LoggerSetupFactory.provider(loggerImplementation).getLogger(loggerUrl, clientName);
	}

	/**
	 * Displays a summary of the current configuration.
	 */
	private static void displayConfiurationSummary(){
		System.out.println("\nConfiguration Summary");	
		System.out.println("---------------------");	
		System.out.println("Logger implementation: " + loggerImplementation);
		System.out.println("Server host: " + serverHost);
		System.out.println("Server port: " + serverPort);		
		System.out.println("Client name: " + clientName);
		System.out.println("Logger URL: " + loggerUrl + "\n");
	}

	/**
	 * The main method, serving as the entry point for the application.
	 *
	 * @param args Command-line arguments (not used).
	 */
    public static void main(final String[] args) {
		try (Scanner scanner = new Scanner(System.in)) {
			boolean running = true;

			while (running) {
				Logger logger = configureLogger(scanner);
				displayConfiurationSummary();
				System.out.println("Logger successfully configured.\nYou can now log messages!\n");

				String message = "";
				while(!message.equalsIgnoreCase("exit") && !message.equalsIgnoreCase("shutdown")){
					System.out.println("Enter a message to log (log level + message or 'exit' to reconnect, 'config' to show configuration, or 'shutdown' to quit.): ");
					message = scanner.nextLine();
					if (message.equalsIgnoreCase("config")){
						displayConfiurationSummary();
					} else if (!message.equalsIgnoreCase("exit") && !message.equalsIgnoreCase("shutdown")){
						String[] parts = message.split(" ", 2);
						LogLevel level = LogLevel.INFO; // Default log level
						String logMessage = message;

						if (parts.length == 2) {
							try {
								level = LogLevel.valueOf(parts[0].toUpperCase());
								logMessage = parts[1];
							} catch (IllegalArgumentException e) {
								System.out.println("Invalid log level specified. Using default: INFO");
							}
						}

						logger.log(level, logMessage);
					}
				}

				if (message.equalsIgnoreCase("Shutdown")){
					running = false;
					System.out.println("Application is shutting down.");
				} else if (message.equalsIgnoreCase("exit")) {
					System.out.println("\nConnection closed. Configuring new logger connection...\n");
				}
			}
		} catch (Exception e){
			System.err.println("An error occured during configuration process: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
