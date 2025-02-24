package ch.hslu.vsk.logger.common.formatter;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;


class HumanReadableLogMessageFormatterTest {

	@Test
	void serializeValidLogMessage() {
		LogMessage message = new LogMessage(Instant.now(), Instant.now(), "Default", LogLevel.INFO, "Test message");
		LogMessageFormatter formatter = new HumanReadableLogMessageFormatter();
		String result = formatter.serialize(message);
		assertEquals("ReceivedAt: [" + InstantSerializer.stringifyInstant(message.getReceivedAtServer()) + "]\nTimestamp: [" + InstantSerializer.stringifyInstant(message.getTimestamp()) + "]\nClient-Name: [Default]\nLog-Level: [INFO]\nMessage: [Test message]", result);
	}

	@Test
	void parseValidLogMessageString() {
		Instant now = Instant.now();
		String logMessageString = "ReceivedAt: [" + InstantSerializer.stringifyInstant(now) + "]\nTimestamp: [" + InstantSerializer.stringifyInstant(now) + "]\nClient-Name: [Default]\nLog-Level: [INFO]\nMessage: [Test message]";
		HumanReadableLogMessageFormatter formatter = new HumanReadableLogMessageFormatter();
		LogMessage result = formatter.parse(logMessageString);
		LogMessage expected = new LogMessage(now, now, "Default", LogLevel.INFO, "Test message");
		assertEquals(expected, result);
	}

	@Test
	void parseInvalidString() {
		String logMessageString = "Timestamp: [invalid-timestamp]\nLog-Level: [INFO]\nMessage: [Test message]";
		HumanReadableLogMessageFormatter formatter = new HumanReadableLogMessageFormatter();
		assertThrows(IllegalArgumentException.class, () -> formatter.parse(logMessageString));
	}


	@Test
	void parseAndSerializeConsistency() {
		LogMessage message = new LogMessage(Instant.now(), Instant.now(), "Default", LogLevel.INFO, "Test message");
		LogMessageFormatter formatter = new HumanReadableLogMessageFormatter();
		String serializedMessage = formatter.serialize(message);
		LogMessage parsedMessage = formatter.parse(serializedMessage);
		String reSerializedMessage = formatter.serialize(parsedMessage);
		assertEquals(serializedMessage, reSerializedMessage);
	}
}