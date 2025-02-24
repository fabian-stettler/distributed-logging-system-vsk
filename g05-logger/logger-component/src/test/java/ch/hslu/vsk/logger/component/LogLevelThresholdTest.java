package ch.hslu.vsk.logger.component;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;

class LogLevelThresholdTest {

    private List<LogMessage> sentMessages = new ArrayList<>();

    private LoggerComponent createLogger(LogLevel threshold) {
        return new LoggerComponent(message -> sentMessages.add(message), "DemoLogger", threshold);
    }

    @BeforeEach
    void beforeEach() {
        sentMessages.clear();
    }

    @Test
    void testNoLogLevelThreshold() {
        LoggerComponent logger = createLogger(null);
        logger.debug("Hello");
        logger.info("test");
        logger.warning("Test");
        logger.error("Oh no");

        assertThat(sentMessages).hasSize(4);
    }

    @Test
    void testInfoThreshold() {
        LoggerComponent logger = createLogger(LogLevel.INFO);
        logger.debug("Hello");
        logger.info("test");
        logger.warning("Test");
        logger.error("Oh no");

        assertThat(sentMessages).hasSize(3); // Only 3 should be sent
        assertThat(sentMessages.stream().filter(message -> message.getLogLevel() == LogLevel.DEBUG).count()).isZero(); // The LogLevel DEBUG should not have been sent
    }

    @Test
    void testWarningThreshold() {
        LoggerComponent logger = createLogger(LogLevel.WARNING);
        logger.debug("Debug Message");
        logger.info("Info Message");
        logger.warning("Warning Message");
        logger.error("Error Message");

        assertThat(sentMessages).hasSize(2); // Only WARNING and ERROR should be sent
        assertThat(sentMessages.stream().filter(message -> message.getLogLevel() == LogLevel.WARNING).count()).isEqualTo(1);
        assertThat(sentMessages.stream().filter(message -> message.getLogLevel() == LogLevel.ERROR).count()).isEqualTo(1);
    }

    @Test
    void testErrorThreshold() {
        LoggerComponent logger = createLogger(LogLevel.ERROR);
        logger.debug("Debug Message");
        logger.info("Info Message");
        logger.warning("Warning Message");
        logger.error("Error Message");

        assertThat(sentMessages).hasSize(1); // Only ERROR should be sent
        assertThat(sentMessages.stream().filter(message -> message.getLogLevel() == LogLevel.ERROR).count()).isEqualTo(1);
    }

    @Test
    void testDebugThreshold() {
        LoggerComponent logger = createLogger(LogLevel.DEBUG);
        logger.debug("Debug Message");
        logger.info("Info Message");
        logger.warning("Warning Message");
        logger.error("Error Message");

        assertThat(sentMessages).hasSize(4); // All messages should be sent
    }

    @Test
    void testNullMessageLogging() {
        LoggerComponent logger = createLogger(LogLevel.INFO);
		logger.info(null);
        assertThat(sentMessages).hasSize(1);
    }
}