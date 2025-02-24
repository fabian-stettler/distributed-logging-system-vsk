package ch.hslu.vsk.logger.server.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesFileConfigSourceTest {

	private static final String TEST_PROPERTIES_FILE = "./test_config.properties";

	@BeforeAll
	public static void setup() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("LISTEN_PORT", "1234");
		properties.setProperty("LOG_FILE", "my_log.test");

		try (FileWriter writer = new FileWriter(TEST_PROPERTIES_FILE)) {
			properties.store(writer, "Test properties");
		}
	}

	@AfterAll
	public static void cleanup() throws IOException {
		Files.delete(Path.of(TEST_PROPERTIES_FILE));
	}

	@Test
	void testGetConfigValueWithValidProperty() {
		PropertiesFileConfigSource source = new PropertiesFileConfigSource(TEST_PROPERTIES_FILE);

		assertThat(source.getConfigValue("listen_port")).isEqualTo("1234");
		assertThat(source.getConfigValue("log_file")).isEqualTo("my_log.test");
	}

	@Test
	void testGetConfigValueWithInvalidProperty() {
		PropertiesFileConfigSource source = new PropertiesFileConfigSource(TEST_PROPERTIES_FILE);

		assertThat(source.getConfigValue("im_non_existent")).isNull();
	}

}