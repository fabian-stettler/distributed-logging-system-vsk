package ch.hslu.vsk.logger.server.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ArgsConfigSourceTest {

	@Test
	void testGetConfigValueWithValidArg() {
		String[] args = {"--listen-port=HelloWorld", "--log-file=my_test.log"};
		ArgsConfigSource source = new ArgsConfigSource(args);

		assertThat(source.getConfigValue("listen_port")).isEqualTo("HelloWorld");
		assertThat(source.getConfigValue("log_file")).isEqualTo("my_test.log");
	}

	@Test
	void testGetConfigValueWithInvalidArg() {
		String[] args = {"--listen-port=123"};
		ArgsConfigSource source = new ArgsConfigSource(args);

		assertThat(source.getConfigValue("log_file")).isNull();
	}

}