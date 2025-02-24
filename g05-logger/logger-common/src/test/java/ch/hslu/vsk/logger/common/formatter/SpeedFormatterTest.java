package ch.hslu.vsk.logger.common.formatter;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

class SpeedFormatterTest {

    @Test
    void serializeValidLogMessage() {

		Instant sentAt = Instant.now();
		Instant receivedAt = Instant.now().plusSeconds(1);

        LogMessage message = new LogMessage(receivedAt, sentAt, "HelloLogger", LogLevel.INFO, "Test message");
        SpeedFormatter formatter = new SpeedFormatter();
        String result = formatter.serialize(message);
        assertEquals("ReceivedAt: " + InstantSerializer.stringifyInstant(receivedAt) + " ||| Timestamp: " + InstantSerializer.stringifyInstant(sentAt) + " ||| Client-Name: " + message.getClientName() + " ||| Log-Level: INFO ||| Message: Test message", result);
    }

    @Test
    void parseValidLogMessageString() {
		Instant sentAt = Instant.now();
		Instant receivedAt = Instant.now().plusSeconds(1);

        String logMessageString = "ReceivedAt: " + InstantSerializer.stringifyInstant(receivedAt) + " ||| Timestamp: " + InstantSerializer.stringifyInstant(sentAt) + " ||| Client-Name: MyLogger ||| Log-Level: INFO ||| Message: Test message";
        SpeedFormatter formatter = new SpeedFormatter();
        LogMessage result = formatter.parse(logMessageString);
		LogMessage expected = new LogMessage(receivedAt, sentAt, "MyLogger", LogLevel.INFO, "Test message");
        assertEquals(expected, result);
    }

    @Test
    void parseInvalidTimestampString() {
        String logMessageString = "Timestamp: invalid-timestamp ||| Log-Level: INFO ||| Message: Test message";
        SpeedFormatter formatter = new SpeedFormatter();
        assertThrows(IllegalArgumentException.class, () -> formatter.parse(logMessageString));
    }

    @Test
    void parseMissingFieldsString() {
        String logMessageString = "Timestamp: " + InstantSerializer.stringifyInstant(Instant.now()) + " ||| Log-Level: INFO";
        SpeedFormatter formatter = new SpeedFormatter();
        assertThrows(IllegalArgumentException.class, () -> formatter.parse(logMessageString));
    }
}