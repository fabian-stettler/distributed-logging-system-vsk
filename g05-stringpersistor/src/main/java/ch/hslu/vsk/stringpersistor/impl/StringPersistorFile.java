package ch.hslu.vsk.stringpersistor.impl;

import ch.hslu.vsk.stringpersistor.api.PersistedString;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the {@link StringPersistor} interface for saving and retrieving strings to a file.
 * This implementation uses a simple comma-separated format to persist strings along with their timestamp.
 * The class supports appending new entries to the file and retrieving them efficiently.
 */
public class StringPersistorFile implements StringPersistor, AutoCloseable {

    private Path path;
    public static final String DELIMITER = ",";

    // File writer for saving strings
    private BufferedWriter writer;

    /**
     * Sets the file path where strings will be saved.
     * 
     * This method is used to specify the file that will be used for persisting strings.
     * If the file doesn't exist, it will be created.
     * 
     * @param path The {@link Path} to the file where the strings should be saved.
     * @throws IllegalArgumentException if the path is invalid.
     */
    @Override
    public void setFile(Path path) {
        this.path = path;

		// Close previous handle
		this.close();
		
        try {

			// Create file if it doesn't exist
            if (Files.notExists(path)) {
                Files.createFile(path);
            }

			File file = path.toFile();

			if (!file.canRead()) {
				throw new SecurityException("Cannot read from the file");
			}
			if (!file.canWrite()) {
				throw new SecurityException("Cannot write to the file");
			}

            // Open the file for appending
            this.writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to set file: " + e.getMessage());
        }
    }

    /**
     * Saves a string along with its timestamp to the specified file.
     * 
     * This method appends the given string and timestamp to the file in the format "timestamp,line".
     * If either the timestamp or the line is {@code null}, an {@link IllegalArgumentException} will be thrown.
     * 
     * @param instant The {@link Instant} timestamp to be saved.
     * @param line The string content to be saved.
     * @throws IllegalArgumentException if {@code instant} or {@code line} is {@code null}.
     * @throws IllegalStateException if the file path is not set or an I/O error occurs.
     */
    @Override
    public void save(Instant instant, String line) {
        if (instant == null || line == null) {
            throw new IllegalArgumentException("Instant and line must not be null");
        }
        if (this.path == null) {
            throw new IllegalStateException("Failed to save line: path not specified");
        }

        try {
            String serializedLine = instant.toString() + DELIMITER + line;
            writer.write(serializedLine);
			writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save line: " + e.getMessage());
        }
    }

    /**
     * Retrieves up to a specified number of lines from the file.
     * 
     * This method reads the file and retrieves the first {@code i} lines, parsing them into {@link PersistedString}
     * objects containing the timestamp and line content. If {@code i} is {@code Integer.MAX_VALUE}, it retrieves all lines.
     * 
     * @param i The maximum number of lines to retrieve from the file.
     * @return A list of {@link PersistedString} objects containing the timestamp and line content.
     * @throws IllegalStateException if the file path is not set or an I/O error occurs.
     */
    @Override
    public List<PersistedString> get(int i) {
        if (i <= 0) {
            return Collections.emptyList();
        }

        if (this.path == null) {
            throw new IllegalStateException("Failed to get line: path not specified");
        }

        try (Stream<String> linesStream = Files.lines(this.path)) {
            // Limit the stream to the specified number of lines
            Stream<String> limitedStream = (i == Integer.MAX_VALUE) ? linesStream : linesStream.limit(i);

            // Convert each line to PersistedString and collect into a list
            return limitedStream
                    .map(line -> {
                        String[] split = line.split(DELIMITER, 2);
                        return new PersistedString(Instant.parse(split[0]), split[1]);
                    })
                    .collect(Collectors.toList()); // Collect to list in memory
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read logs: " + e.getMessage());
        }
    }

    /**
     * Closes the {@link BufferedWriter} when no longer needed.
     * 
     * It is important to close the writer after finishing all write operations to free system resources.
     * 
     * @throws IllegalStateException if an I/O error occurs during the closing of the writer.
     */
    public void closeWriter() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close writer: " + e.getMessage());
        }
    }


	@Override
	public void close() {
		if (this.writer != null) {
			try {
				this.writer.close();
			} catch (IOException ex) {
				System.out.println("Error while closing writer");
			}
		}
	}
}