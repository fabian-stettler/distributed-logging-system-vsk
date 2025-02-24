package ch.hslu.vsk.logger.common.formatter;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

/**
 * This class contains tests for comparing the performance of the {@code SpeedFormatter} against
 * XML and JSON formatters in terms of serialization and deserialization speeds.
 * The tests are parameterized to allow different operation counts for benchmarking.
 */
class SpeedTestingTest {

    /**
     * This test compares the serialization speed of the {@code SpeedFormatter} with XML and JSON formatters.
     * It performs the specified number of serialization operations on a {@code LogMessage} and measures
     * the time taken by each formatter.
     * The test outputs a warning if the {@code SpeedFormatter} is slower than expected, which is defined
     * as taking longer than half the combined time of XML and JSON formatters.
     *
     * @param operationCount The number of serialization operations to perform.
     */
    @ParameterizedTest
    @CsvSource({ "500_000" })
    void testSpeedSerialize(final int operationCount) {
        LogMessage message = new LogMessage(Instant.now().plusSeconds(1), Instant.now(), "MyLogger", LogLevel.INFO, "Test message");
        LogMessageFormatter xmlFormatter = new XMLLogMessageFormatter();
        LogMessageFormatter jsonFormatter = new JSONLogMessageFormatter();
        LogMessageFormatter speedFormatter = new SpeedFormatter();

        // Measure serialization time for XML format
        long start = System.currentTimeMillis();
        for (int i = 0; i < operationCount; i++) {
            xmlFormatter.serialize(message);
        }
        long xmlTime = System.currentTimeMillis() - start;

        // Measure serialization time for JSON format
        start = System.currentTimeMillis();
        for (int i = 0; i < operationCount; i++) {
            jsonFormatter.serialize(message);
        }
        long jsonTime = System.currentTimeMillis() - start;

        // Measure serialization time for SpeedFormatter
        start = System.currentTimeMillis();
        for (int i = 0; i < operationCount; i++) {
            speedFormatter.serialize(message);
        }
        long speedTime = System.currentTimeMillis() - start;

        // Warning if SpeedFormatter is slower than expected
        if (speedTime > (jsonTime + xmlTime) / 2) {
            System.out.println("WARNING: SpeedFormatter is slower than expected.");
        }
    }

    /**
     * This test compares the deserialization speed of the {@code SpeedFormatter} with XML and JSON formatters.
     * It performs the specified number of deserialization operations for each format and measures the time taken.
     * The test outputs a warning if the {@code SpeedFormatter} is slower than expected, which is defined
     * as taking longer than half the combined time of XML and JSON formatters.
     *
     * @param operationCount The number of deserialization operations to perform.
     */
    @ParameterizedTest
    @CsvSource({ "500_000" })
    void testSpeedParse(final int operationCount) {
        LogMessage message = new LogMessage(Instant.now().plusSeconds(1), Instant.now(), "MyLogger", LogLevel.INFO, "Test message");
        LogMessageFormatter xmlFormatter = new XMLLogMessageFormatter();
        LogMessageFormatter jsonFormatter = new JSONLogMessageFormatter();
        LogMessageFormatter speedFormatter = new SpeedFormatter();

        // Serialize messages for each format
        String xmlMessage = xmlFormatter.serialize(message);
        String jsonMessage = jsonFormatter.serialize(message);
        String speedMessage = speedFormatter.serialize(message);

        // Measure deserialization time for XML format
        long start = System.currentTimeMillis();
        for (int i = 0; i < operationCount; i++) {
            xmlFormatter.parse(xmlMessage);
        }
        long xmlTime = System.currentTimeMillis() - start;

        // Measure deserialization time for JSON format
        start = System.currentTimeMillis();
        for (int i = 0; i < operationCount; i++) {
            jsonFormatter.parse(jsonMessage);
        }
        long jsonTime = System.currentTimeMillis() - start;

        // Measure deserialization time for SpeedFormatter
        start = System.currentTimeMillis();
        for (int i = 0; i < operationCount; i++) {
            speedFormatter.parse(speedMessage);
        }
        long speedTime = System.currentTimeMillis() - start;

        // Warning if SpeedFormatter is slower than expected
        if (speedTime > (jsonTime + xmlTime) / 2) {
            System.out.println("WARNING: SpeedFormatter is slower than expected.");
        }
    }
}
