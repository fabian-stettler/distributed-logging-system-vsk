package ch.hslu.vsk.logger.common.formatter;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

class XMLLogMessageFormatterTest {

    @Test
    void serializeValidLogMessage() {
		Instant sentAt = Instant.now();
		Instant receivedAt = Instant.now().plusSeconds(1);

        LogMessage message = new LogMessage(receivedAt, sentAt, "Logger", LogLevel.INFO, "Test message");
        LogMessageFormatter formatter = new XMLLogMessageFormatter();
        String result = formatter.serialize(message);
        assertEquals("<logMessage><receivedAt>" + InstantSerializer.stringifyInstant(receivedAt) + "</receivedAt><timestamp>" + InstantSerializer.stringifyInstant(sentAt) + "</timestamp><clientName>" + message.getClientName() + "</clientName><logLevel>INFO</logLevel><message>Test message</message></logMessage>", result);
    }

    @Test
    void parseValidXMLString() {
		Instant sentAt = Instant.now();
		Instant receivedAt = Instant.now().plusSeconds(1);

        String xml = "<logMessage><receivedAt>" + InstantSerializer.stringifyInstant(receivedAt) + "</receivedAt><timestamp>" + InstantSerializer.stringifyInstant(sentAt) + "</timestamp><clientName>Loggy</clientName><logLevel>INFO</logLevel><message>Test message</message></logMessage>";
        LogMessageFormatter formatter = new XMLLogMessageFormatter();
        LogMessage result = formatter.parse(xml);
		LogMessage expected = new LogMessage(receivedAt, sentAt, "Loggy", LogLevel.INFO, "Test message");
        assertEquals(expected, result);
    }

    @Test
    void parseInvalidXMLString() {
        String xml = "<logMessage><timestamp>invalid-timestamp</timestamp><logLevel>INFO</logLevel><message>Test message</message></logMessage>";
        LogMessageFormatter formatter = new XMLLogMessageFormatter();
        assertThrows(IllegalArgumentException.class, () -> formatter.parse(xml));
    }

    @Test
    void parseMissingMessageFieldXMLString() {
		Instant sentAt = Instant.now();

        String xml = "<logMessage><timestamp>" + InstantSerializer.stringifyInstant(sentAt) + "</timestamp><logLevel>INFO</logLevel></logMessage>";
        LogMessageFormatter formatter = new XMLLogMessageFormatter();
        assertThrows(IllegalArgumentException.class, () -> formatter.parse(xml));
    }

    @Test
    void parseAndSerializeConsistency() {
        LogMessage message = new LogMessage(Instant.now().plusSeconds(1), Instant.now(), "HelloLogger", LogLevel.INFO, "Test message");
        LogMessageFormatter formatter = new XMLLogMessageFormatter();
        String serializedMessage = formatter.serialize(message);
        LogMessage parsedMessage = formatter.parse(serializedMessage);
        String reSerializedMessage = formatter.serialize(parsedMessage);
        assertEquals(serializedMessage, reSerializedMessage);
    }

    @Test
    void parseStringWithDelimeterInMessage() {
		
		Instant sentAt = Instant.now();
		Instant receivedAt = Instant.now().plusSeconds(1);

        String xml = "<logMessage><receivedAt>" + InstantSerializer.stringifyInstant(receivedAt) + "</receivedAt><timestamp>" + InstantSerializer.stringifyInstant(sentAt) + "</timestamp><clientName>MyLogger</clientName><logLevel>INFO</logLevel><message>Test &lt;message&gt;</message></logMessage>";
        LogMessageFormatter formatter = new XMLLogMessageFormatter();
        LogMessage result = formatter.parse(xml);
		LogMessage expected = new LogMessage(receivedAt, sentAt, "MyLogger", LogLevel.INFO, "Test <message>");
        assertEquals(expected, result);
    }


    @Test
    void serializeStringWithDelimeterInMessage() {
        LogMessage message = new LogMessage(Instant.now().plusSeconds(1), Instant.now(), "MyLogger", LogLevel.INFO, "Test<>message");
        LogMessageFormatter formatter = new XMLLogMessageFormatter();
        assertEquals("<logMessage><receivedAt>" + InstantSerializer.stringifyInstant(message.getReceivedAtServer()) + "</receivedAt><timestamp>" + InstantSerializer.stringifyInstant(message.getTimestamp()) + "</timestamp><clientName>MyLogger</clientName><logLevel>INFO</logLevel><message>Test&lt;&gt;message</message></logMessage>", formatter.serialize(message));
    }
}