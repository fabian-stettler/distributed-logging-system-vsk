package ch.hslu.vsk.logger.common.formatter;

import ch.hslu.vsk.logger.common.LogMessage;

/**
 * Defines the contract for formatting and parsing log messages.
 * Implementations of this interface handle the conversion of log messages
 * between structured objects and serialized representations.
 */
public interface LogMessageFormatter {

    /**
     * Serializes a {@link LogMessage} object into a string representation.
     *
     * @param message the {@link LogMessage} object to serialize; must not be null.
     * @return the serialized string representation of the log message.
     * @throws IllegalArgumentException if the provided {@link LogMessage} is invalid or null.
     */
    String serialize(LogMessage message);

    /**
     * Parses a string representation of a log message into a {@link LogMessage} object.
     *
     * @param message the string representation of the log message to parse; must not be null or empty.
     * @return a {@link LogMessage} object parsed from the given string.
     * @throws IllegalArgumentException if the parsing fails due to an invalid or malformed input string.
     */
    LogMessage parse(String message);

}

