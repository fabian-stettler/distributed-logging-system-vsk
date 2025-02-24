package ch.hslu.vsk.logger.common;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.network.NetworkMessageInputStream;
import ch.hslu.vsk.logger.common.network.NetworkMessageOutputStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

class LogMessageSerializationTest {

	/**
	 * Test that a Person can be serialized and then deserialized correctly.
	 */
	@Test
	void testSerializationAndDeserialization() {

		LogMessage message = new LogMessage(Instant.now(), Instant.now().plusSeconds(1), "Client Name", LogLevel.INFO, "Some message");

		try {
			// Serialize the person object
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			NetworkMessageOutputStream networkMessageOutputStream = new NetworkMessageOutputStream(byteArrayOutputStream);
			message.serialize(networkMessageOutputStream);

			// Now, deserialize it back
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			NetworkMessageInputStream networkMessageInputStream = new NetworkMessageInputStream(byteArrayInputStream);

			LogMessage deserializedLogMessage = (LogMessage) networkMessageInputStream.nextObject();

			// Assert that the deserialized person is the same as the original person
			assertThat(deserializedLogMessage).isNotNull();
			assertThat(deserializedLogMessage).isEqualTo(message);
		} catch (IOException e) {
			fail("Serialization or deserialization failed: " + e.getMessage());
		}
	}

}
