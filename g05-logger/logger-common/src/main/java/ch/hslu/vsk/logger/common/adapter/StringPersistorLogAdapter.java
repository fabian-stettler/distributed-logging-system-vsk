package ch.hslu.vsk.logger.common.adapter;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.formatter.LogMessageFormatter;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;

/**
 * A thread-safe adapter for persisting log messages using a {@link StringPersistor}.
 */
public final class StringPersistorLogAdapter implements LogAdapter {

	private static final Logger LOGGER = LogManager.getLogger(StringPersistorLogAdapter.class);

    private final StringPersistor stringPersistor;
    private final LogMessageFormatter formatter;
    private final Lock lock = new ReentrantLock();

    /**
     * Constructs a new {@code StringPersistorLogAdapter} with the specified string persistor and formatter.
     *
     * @param stringPersistor the {@link StringPersistor} used for saving and retrieving log messages; must not be null.
     * @param formatter       the {@link LogMessageFormatter} used for serializing and deserializing log messages; must not be null.
     */
    public StringPersistorLogAdapter(final StringPersistor stringPersistor, final LogMessageFormatter formatter) {
        if (stringPersistor == null || formatter == null) {
            throw new IllegalArgumentException("StringPersistor and LogMessageFormatter must not be null.");
        }

        this.stringPersistor = stringPersistor;
        this.formatter = formatter;
    }

    /**
     * Saves a {@link LogMessage} by serializing it and persisting it with a timestamp.
     *
     * @param logMessage the {@link LogMessage} to save; must not be null.
     * @return {@code true} if the log message was successfully saved, {@code false} otherwise.
     */
    @Override
    public boolean saveLogMessage(final LogMessage logMessage) {
        if (logMessage == null) {
            return false;
        }

        try {
            String serializedMessage = formatter.serialize(logMessage);
        	lock.lock();
            stringPersistor.save(Instant.now(), serializedMessage);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
			LOGGER.error("Failed to save LogMessage", e);
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Loads the specified number of log messages, deserializing them into {@link LogMessage} objects.
     * Any lines that fail to parse are skipped.
     *
     * @param count the maximum number of log messages to load; must be greater than 0.
     * @return a list of successfully parsed {@link LogMessage} objects.
     */
    @Override
    public List<LogMessage> loadLogMessages(final int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0.");
        }

        lock.lock();
        try {
            return stringPersistor.get(count).parallelStream()
                    .map(line -> {
                        try {
                            return formatter.parse(line.getPayload());
                        } catch (IllegalArgumentException e) {
                            // Skip lines that fail to parse
                            return null;
                        }
                    })
                    .filter(Objects::nonNull) // Remove null entries
                    .toList();
		} catch (Throwable ex) {
			LOGGER.error("Error while reading log messages", ex);
			return Collections.emptyList();
        } finally {
            lock.unlock();
        }
    }
}
