package ch.hslu.vsk.logger.common.formatter;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;


class JSONLogMessageFormatterTest {

    @Test
    void serializeValidLogMessage() {

		Instant sentAt = Instant.now();
		Instant receivedAt = Instant.now().plusSeconds(1);

        LogMessage message = new LogMessage(receivedAt, sentAt, "Default", LogLevel.INFO, "Test message");
        LogMessageFormatter formatter = new JSONLogMessageFormatter();
        String result = formatter.serialize(message);
        assertEquals("{\"receivedAt\": \"" + InstantSerializer.stringifyInstant(receivedAt) + "\", \"timestamp\": \"" + InstantSerializer.stringifyInstant(sentAt) + "\", \"clientName\": \"" + message.getClientName() + "\", \"logLevel\": \"INFO\", \"message\": \"Test message\"}", result);
    }

    @Test
    void parseValidJSONStringWithoutInnerQuotes() {

		Instant sentAt = Instant.now();
		Instant receivedAt = Instant.now().plusSeconds(1);

        String json = "{\"receivedAt\": \"" + InstantSerializer.stringifyInstant(receivedAt) + "\", \"timestamp\": \"" + InstantSerializer.stringifyInstant(sentAt) + "\", \"clientName\": \"Default\", \"logLevel\": \"INFO\", \"message\": \"Test message\"}";
        LogMessageFormatter formatter = new JSONLogMessageFormatter();
        LogMessage result = formatter.parse(json);
		LogMessage expected = new LogMessage(receivedAt, sentAt, "Default", LogLevel.INFO, "Test message");
		
		assertEquals(expected, result);
    }

    @Test
    void parseInvalidJSONString() {
        String json = "{\"timestamp\": \"invalid-timestamp\", \"logLevel\": \"INFO\", \"message\": \"Test message\"}";
        LogMessageFormatter formatter = new JSONLogMessageFormatter();
        assertThrows(IllegalArgumentException.class, () -> formatter.parse(json));
    }

    @Test
    void parseAndSerializeConsistency() {
        LogMessage message = new LogMessage(Instant.now(), Instant.now(), "Default", LogLevel.INFO, "Test message");
        LogMessageFormatter formatter = new JSONLogMessageFormatter();
        String serializedMessage = formatter.serialize(message);
        LogMessage parsedMessage = formatter.parse(serializedMessage);
        String reSerializedMessage = formatter.serialize(parsedMessage);
        assertEquals(serializedMessage, reSerializedMessage);
    }

    @Test
    void serializeLogMessageWithQuote() {
        LogMessage message = new LogMessage(Instant.now(), Instant.now(), "Default", LogLevel.INFO, "Test \"message\"");
        LogMessageFormatter formatter = new JSONLogMessageFormatter();
        String result = formatter.serialize(message);
        assertEquals("{\"receivedAt\": \"" + InstantSerializer.stringifyInstant(message.getReceivedAtServer()) + "\", \"timestamp\": \"" + InstantSerializer.stringifyInstant(message.getTimestamp()) + "\", \"clientName\": \"Default\", \"logLevel\": \"INFO\", \"message\": \"Test \\\"message\\\"\"}", result);
    }

    @Test
    void parseLogMessageWithQuote() {
		Instant sentAt = Instant.now();
		Instant receivedAt = Instant.now().plusSeconds(1);

        String json = "{\"receivedAt\": \"" + InstantSerializer.stringifyInstant(receivedAt) + "\", \"timestamp\": \"" + InstantSerializer.stringifyInstant(sentAt) + "\", \"clientName\": \"Test\", \"logLevel\": \"INFO\", \"message\": \"Test \\\"message\\\"\"}";
        LogMessageFormatter formatter = new JSONLogMessageFormatter();
        LogMessage result = formatter.parse(json);
        assertEquals("Test \"message\"", result.getMessage());
    }

}