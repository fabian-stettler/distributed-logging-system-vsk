package ch.hslu.vsk.logger.component.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.adapter.LogAdapter;
import ch.hslu.vsk.logger.common.adapter.StringPersistorLogAdapter;
import ch.hslu.vsk.logger.common.formatter.LogMessageFormat;
import ch.hslu.vsk.logger.common.formatter.LogMessageFormatterFactory;
import ch.hslu.vsk.logger.common.util.FileCreator;
import ch.hslu.vsk.stringpersistor.api.StringPersistor;
import ch.hslu.vsk.stringpersistor.impl.StringPersistorFile;

/**
 * A file-based implementation of the {@link LogMessageCacheStrategy} interface.
 * Uses a file system to persist, retrieve, and clear log messages.
 */
public final class FileCacheStrategy implements LogMessageCacheStrategy {

    private final LogAdapter logAdapter;
    private final Path filePath;

    /**
     * Constructs a new {@code FileCacheStrategy} with the specified file path.
     *
     * @param filePath the file path for storing cached log messages; must not be null.
     * @throws IllegalArgumentException if the file path is null.
     */
    public FileCacheStrategy(final Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path must not be null.");
        }

        this.filePath = filePath;

		// Ensure the file exists
		if (!FileCreator.createFile(filePath)) {
			throw new IllegalArgumentException("Invalid filePath. Failed to create the file");
		}

        StringPersistor persistor = new StringPersistorFile();
        persistor.setFile(filePath);

        this.logAdapter = new StringPersistorLogAdapter(
            persistor,
            LogMessageFormatterFactory.createFormatter(LogMessageFormat.SPEED_FORMAT)
        );
    }

    @Override
    public boolean save(final LogMessage message) {
        return logAdapter.saveLogMessage(message);
    }

    @Override
    public List<LogMessage> retrieveAll() {
        return logAdapter.loadLogMessages(Integer.MAX_VALUE);
    }

    @Override
    public boolean clear() {
        try {
            Files.write(filePath, new byte[0]);
            return true;
        } catch (IOException e) {
            // Log the error if necessary (not implemented for brevity)
            return false;
        }
    }
}

