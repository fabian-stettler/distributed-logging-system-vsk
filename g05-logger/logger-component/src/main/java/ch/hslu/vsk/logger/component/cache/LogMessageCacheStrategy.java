package ch.hslu.vsk.logger.component.cache;

import java.util.List;

import ch.hslu.vsk.logger.common.LogMessage;

/**
 * Strategy interface for caching log messages.
 * Defines the contract for saving, retrieving, and clearing log messages.
 */
public interface LogMessageCacheStrategy {

	/**
	 * Saves a log message using the caching strategy.
	 *
	 * @param message the {@link LogMessage} to save; must not be null.
	 * @return {@code true} if the message was successfully saved, {@code false} otherwise.
	 */
	boolean save(LogMessage message);

	/**
	 * Retrieves all cached log messages using the caching strategy.
	 *
	 * @return a list of {@link LogMessage} objects retrieved from the cache.
	 */
	List<LogMessage> retrieveAll();

	/**
	 * Clears all cached log messages using the caching strategy.
	 *
	 * @return {@code true} if the cache was successfully cleared, {@code false} otherwise.
	 */
	boolean clear();
}

