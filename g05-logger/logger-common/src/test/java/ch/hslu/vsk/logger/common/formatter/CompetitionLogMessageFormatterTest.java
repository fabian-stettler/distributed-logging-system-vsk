package ch.hslu.vsk.logger.common.formatter;

import org.junit.jupiter.api.Test;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;

class CompetitionLogMessageFormatterTest {

    private final CompetitionLogMessageFormatter formatter = new CompetitionLogMessageFormatter();

    @Test
    void testSerialize() {
        // Sample data
        Instant timestamp = Instant.parse("2024-12-05T10:15:30.123456Z");
        LogLevel logLevel = LogLevel.INFO;
        String clientName = "Client1";
        String messageContent = "Test message";
        
        // Create a LogMessage object
        LogMessage message = new LogMessage(timestamp, clientName, logLevel, messageContent);
        
        // Expected formatted message
        String expected = "2024-12-05 10:15:30.1234 INFO Client1 Test message";
        
        // Serialize the message
        String serializedMessage = formatter.serialize(message);
        
        // Assert that the serialized message matches the expected string
        assertEquals(expected, serializedMessage);
    }

    @ParameterizedTest
    @CsvSource({
            "'2024-12-05 10:15:30.1234 INFO Test', Missing client name",
            "'2024-12-05 10:15:30.1234 INVALID Client1 Test message', Invalid log level",
            "'2024-12-05 10:15:30 INVALID INFO Client1 Test message', Invalid timestamp format"
    })
    void testParse_InvalidInputs(String invalidInput, String description) {
        assertThrows(IllegalArgumentException.class, () -> {
            formatter.parse(invalidInput);
        }, "Expected IllegalArgumentException for: " + description);
    }

    @Test
     void testParse_InvalidTimestamp() {
        // Invalid timestamp format
        String invalidInput = "2024-12-05 10:15:30 INVALID INFO Client1 Test message";
        
        // Test for IllegalArgumentException due to invalid timestamp
        assertThrows(IllegalArgumentException.class, () -> {
            formatter.parse(invalidInput);
        });
    }
}

