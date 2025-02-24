package ch.hslu.vsk.logger.server.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvConfigSourceTest {

	private static Path tempEnvFile;

	@BeforeAll
	public static void setup() throws IOException {
		// Create a temporary .env file for testing
		tempEnvFile = Files.createFile(Path.of("./test.env"));

		// Write test environment variables to the file
		String content = "LISTEN_PORT=1234\nLOG_FILE=my_test.log";
		Files.writeString(tempEnvFile, content, StandardOpenOption.WRITE);
	}

	@AfterAll
	public static void cleanup() throws IOException {
		// Delete the temporary .env file after each test
		Files.deleteIfExists(tempEnvFile);
	}

	@Test
	void testGetConfigValueWithValidEnv() {
		// Use the tempEnvFile path to initialize EnvConfigSource
		EnvConfigSource source = new EnvConfigSource(tempEnvFile);

		assertThat(source.getConfigValue("listen_port")).isEqualTo("1234");
		assertThat(source.getConfigValue("log-file")).isEqualTo("my_test.log");
	}

	@Test
	void testGetConfigValueWithInvalidEnv() {
		EnvConfigSource source = new EnvConfigSource(tempEnvFile);

		assertThat(source.getConfigValue("im-a-key-without-value")).isNull();
	}
}