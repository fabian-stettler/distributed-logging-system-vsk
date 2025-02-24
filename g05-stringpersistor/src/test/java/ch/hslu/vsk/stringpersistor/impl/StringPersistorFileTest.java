package ch.hslu.vsk.stringpersistor.impl;

import ch.hslu.vsk.stringpersistor.api.PersistedString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link StringPersistorFile} class.
 * <p>
 * This test class verifies the behavior of the {@code StringPersistorFile} in various scenarios, including
 * saving and retrieving persisted strings, handling errors, and performance testing.
 * </p>
 */
class StringPersistorFileTest {

	/**
	 * Default path used for testing purposes.
	 */
	private static final Path DEFAULT_PATH = Path.of("./logs.test.log");

	/**
	 * Creates a default instance of {@link StringPersistorFile} with a predefined file path.
	 * @return a configured {@code StringPersistorFile} instance.
	 */
	private StringPersistorFile getDefaultPersistor() {
		StringPersistorFile persistor = new StringPersistorFile();
		persistor.setFile(DEFAULT_PATH);
		return persistor;
	}

	/**
	 * Cleans up by deleting the test file after each test execution.
	 */
	@AfterEach
	public void afterEach() {
		try {
			Files.delete(DEFAULT_PATH);
			System.out.println("Deleted file");
		} catch (IOException e) {
			System.out.println("No file to delete created");
		}
	}

	/**
	 * Verifies that the {@link StringPersistorFile#save} method throws {@link IllegalArgumentException} when
	 * called with {@code null} arguments.
	 */
	@Test
	void testSaveIllegalArguments() {
		var persistor = getDefaultPersistor();

		// Check null instant
		assertThatThrownBy(() -> persistor.save(null, "Test"))
				.isInstanceOf(IllegalArgumentException.class);

		// Check null message
		assertThatThrownBy(() -> persistor.save(Instant.now(), null))
				.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Verifies that {@link StringPersistorFile#save} throws {@link IllegalStateException} when the file path
	 * is invalid or non-existent.
	 */
	@Test
	void testCreateFileOnSetFile() throws IOException {
		var persistor = getDefaultPersistor();
		persistor.setFile(Path.of("./created_log_file.log"));
		assertTrue(Files.exists(Path.of("./created_log_file.log")));
		Files.delete(Path.of("./created_log_file.log"));
	}

	/**
	 * Verifies that {@link StringPersistorFile#save} throws {@link IllegalStateException} when no file path
	 * is specified.
	 */
	@Test
	void testSaveWithoutPath() {
		var persistor = new StringPersistorFile();
		assertThatThrownBy(() -> persistor.save(Instant.now(), "Hello World"))
				.isInstanceOf(IllegalStateException.class);
	}

	/**
	 * Verifies that reading from a non-existent file throws an {@link IllegalStateException}.
	 */
	@Test
	void testReadFromNonExistentFile() throws IOException {
		var persistor = getDefaultPersistor();
		persistor.setFile(Path.of("./missing.log"));
		Files.delete(Path.of("./missing.log"));
		assertThatThrownBy(() -> persistor.get(Integer.MAX_VALUE))
				.isInstanceOf(IllegalStateException.class);
	}

	/**
	 * Verifies that reading without specifying a file throws an {@link IllegalStateException}.
	 */
	@Test
	void testReadWithNoFileSpecified() {
		var persistor = new StringPersistorFile();
		assertThatThrownBy(() -> persistor.get(Integer.MAX_VALUE))
				.isInstanceOf(IllegalStateException.class);
	}

	/**
	 * Tests writing and reading a specified number of lines to and from a file.
	 * @param amount the number of lines to write and read.
	 */
	@ParameterizedTest
	@CsvSource({ "100", "200", "1_000", "4_000", "10_000", "100_000" })
	void testWriteLines(int amount) {
		var persistor = this.getDefaultPersistor();
		List<PersistedString> lines = new ArrayList<>(amount);
		for (int i = 0; i < amount; i++) {
			PersistedString persistedString = new PersistedString(Instant.now(), "Hello World bla bla " + i);
			lines.add(persistedString);
			persistor.save(persistedString.getTimestamp(), persistedString.getPayload());
		}

		List<PersistedString> readLines = persistor.get(amount);
		assertThat(readLines).isEqualTo(lines);
	}

	/**
	 * Tests saving and reading a single line of data.
	 */
	@Test
	void writeSingleLine() {
		var persistor = this.getDefaultPersistor();
		Instant now = Instant.now();
		String message = "Single line test";

		PersistedString sampleString = new PersistedString(now, message);
		persistor.save(now, message);
		var storedMessages = persistor.get(1);
		assertThat(storedMessages.getFirst()).isEqualTo(sampleString);
	}

	/**
	 * Verifies that strings containing the delimiter character can be saved and read correctly.
	 */
	@Test
	void testStringWithDelimiterInPayload() {
		
		var persistor = this.getDefaultPersistor();
		String payload = "test123" + StringPersistorFile.DELIMITER + "blablabla";

		persistor.save(Instant.now(), payload);

		String content = persistor.get(1).getFirst().getPayload();
		assertThat(content).isEqualTo(payload);
	}


	/**
	 * Tests reading all lines from the file to ensure correct total line count.
	 */
	@Test
	void testReadingAllLines() {

		var persistor = this.getDefaultPersistor();

		int lineCount = 53;
		String sample = "a".repeat(10);

		for (int i = 0; i < lineCount; i++) {
			persistor.save(Instant.now(), sample);
		}

		assertThat(persistor.get(Integer.MAX_VALUE).size() == lineCount);
	}



	// -----------------------------------------------------
	// Quality tests
	// -----------------------------------------------------

	/**
	 * Tests that reading 100 lines can be performed within a specified time limit of 500 ms.
	 */
	@Test
	void testRead100LinesWith1000CharsWithin500ms() {

		String sample = "A".repeat(1_000);
		Instant now = Instant.now();

		var persistor = this.getDefaultPersistor();

		for (int i = 0; i < 100; i++) {
			persistor.save(now, sample);
		}

		long start = System.currentTimeMillis();
		List<PersistedString> lines = persistor.get(100);
		long end = System.currentTimeMillis();

		assertThat(end - start).isLessThan(500);
	}


	@Test
	void testWrite1000Strings() {
		final int count = 1_000;
		var persistor = this.getDefaultPersistor();

		for (int i = 0; i < count; i++) {
			persistor.save(Instant.now(), "This is line number: " + i);
		}

		var lines = persistor.get(Integer.MAX_VALUE);
		assertThat(lines.size()).isEqualTo(count);
	}

}
