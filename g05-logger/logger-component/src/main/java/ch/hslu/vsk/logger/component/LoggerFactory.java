package ch.hslu.vsk.logger.component;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.Logger;
import ch.hslu.vsk.logger.api.LoggerSetup;
import ch.hslu.vsk.logger.component.cache.FileCacheStrategy;
import ch.hslu.vsk.logger.component.cache.LogMessageCacheStrategy;
import ch.hslu.vsk.logger.component.sendQueues.BlockingLogMessageSendQueue;
import ch.hslu.vsk.logger.component.sendQueues.LogMessageSendQueue;

/**
 * This class is used to create {@link LoggerComponent} instances.
 */
public final class LoggerFactory implements LoggerSetup  {


	@Override
	public Logger getLogger(final String destination, final String clientName, final LogLevel minLogLevel) {

		Pattern networkPattern = Pattern.compile("([a-zA-Z\\d\\.]+):(\\d+)");
		Matcher matcher = networkPattern.matcher(destination);

		if (!matcher.find()) {
			throw new IllegalArgumentException("Invalid destination");
		}

		// Figure out host and port
		String host = matcher.group(1);
		int port = Integer.parseInt(matcher.group(2));

		LogMessageCacheStrategy cacher = new FileCacheStrategy(Path.of("./tmp/cache_" + clientName + ".log"));
		LogMessageSendQueue queue = new BlockingLogMessageSendQueue(cacher, host, port);

		return new LoggerComponent(queue, clientName, minLogLevel);
	}

	@Override
	public Logger getLogger(final String destination, final String clientName) {
		return this.getLogger(destination, clientName, null);
	}

}
