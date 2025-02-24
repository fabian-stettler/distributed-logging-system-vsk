package ch.hslu.vsk.logger.common.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class InstantSerializerTest {

	@Test
	void testInstantSerialization() {

		Instant instant = Instant.parse("2024-11-28T22:11:56.719904800Z");
		String formatted = InstantSerializer.stringifyInstant(instant);
		assertThat(formatted).isEqualTo("2024-11-28 22:11:56.7199");
	}


	@Test
	void testInstantParsing() {

		String instant = "2024-11-28 22:11:56.7190";
		Instant parsed = InstantSerializer.parseInstant(instant);
		assertThat(parsed).isEqualTo(Instant.parse("2024-11-28T22:11:56.719000000Z"));
	}

	@Test
	void testParsingNull() {
		assertThat(InstantSerializer.parseInstant("NULL")).isNull();
	}

	@Test
	void testInvalidInstant() {
		assertThat(InstantSerializer.parseInstant("INVALID")).isNull();
	}

}