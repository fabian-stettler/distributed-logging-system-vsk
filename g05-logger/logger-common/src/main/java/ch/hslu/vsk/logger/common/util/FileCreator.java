package ch.hslu.vsk.logger.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Utility class for creating files and directories.
 * <p>
 * This class provides methods to ensure the existence of a file
 * by creating its parent directories (if missing) and the file itself.
 * </p>
 */
public final class FileCreator {


	/**
	 * Prevent instantiation.
	 */
	private FileCreator() {
		// Prevent instantiation
	}


	/**
	 * Ensures the existence of a file at the specified path.
	 * If the parent directories or the file do not exist, they will be created.
	 *
	 * @param filePath the {@link Path} to the file, including the file name. Must not be null.
	 * @return {@code true} if the file was created, {@code false} if the file already exists.
	 */
	public static boolean createFile(final Path filePath) {

		if (filePath == null) {
			throw new IllegalArgumentException("The filePath must not be null.");
		}

		try {
			// Ensure the parent directories exist
			if (Files.notExists(filePath.getParent())) {
				Files.createDirectories(filePath.getParent());
			}

			// Don't create it again if it already exists
			if (Files.exists(filePath)) {
				return true;
			}

			// Try to create it
			Files.createFile(filePath);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

}
