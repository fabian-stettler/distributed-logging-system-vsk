package ch.hslu.vsk.logger.common.adapter;

import java.util.List;

import ch.hslu.vsk.logger.common.LogMessage;

/**
 * Provides a unified interface for creating and parsing various log formats.
 * This adapter handles the conversion between {@link LogMessage} objects and their formatted string representations.
 */
public interface LogAdapter {

    /**
     * Saves a {@link LogMessage} object by formatting it into a string and persisting it.
     *
     * @param message the {@link LogMessage} object to save; must not be null.
     * @return {@code true} if the message was successfully saved, {@code false} otherwise.
     */
    boolean saveLogMessage(LogMessage message);

    /**
     * Loads and parses log messages from a source into a list of {@link LogMessage} objects.
     *
     * @param count the maximum number of log messages to load.
     * @return a list of {@link LogMessage} objects parsed from the source.
     */
    List<LogMessage> loadLogMessages(int count);
}

