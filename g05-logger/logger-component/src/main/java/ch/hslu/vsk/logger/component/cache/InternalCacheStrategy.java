package ch.hslu.vsk.logger.component.cache;

import java.util.ArrayList;
import java.util.List;

import ch.hslu.vsk.logger.common.LogMessage;

/**
 * A final implementation of {@link LogMessageCacheStrategy} that provides an in-memory cache for log messages.
 * This class is thread-unsafe and should be used in a single-threaded context or properly synchronized externally.
 */
public final class InternalCacheStrategy implements LogMessageCacheStrategy {

    /**
     * An in-memory list used to store log messages.
     */
    private final List<LogMessage> cache = new ArrayList<>();

    /**
     * Saves a log message to the in-memory cache.
     *
     * @param message the log message to be saved.
     * @return {@code true} if the message was successfully saved.
     */
    @Override
    public boolean save(final LogMessage message) {
        this.cache.add(message);
        return true;
    }

    /**
     * Retrieves all log messages currently stored in the cache.
     *
     * @return a list of all log messages in the cache.
     */
    @Override
    public List<LogMessage> retrieveAll() {
        return new ArrayList<>(this.cache);
    }

    /**
     * Clears all log messages from the cache.
     *
     * @return {@code true} if the cache was successfully cleared.
     */
    @Override
    public boolean clear() {
        this.cache.clear();
        return true;
    }
}
