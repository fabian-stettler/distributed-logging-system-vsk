package ch.hslu.vsk.logger.common.formatter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class LogFormatterFactoryTest {
	
	@Test
	void testLogFormatterInstantiation() {

		Map<LogMessageFormat, Class<? extends LogMessageFormatter>> assignments = new HashMap<>();
		assignments.put(LogMessageFormat.HUMAN_READABLE, HumanReadableLogMessageFormatter.class);
		assignments.put(LogMessageFormat.JSON, JSONLogMessageFormatter.class);
		assignments.put(LogMessageFormat.XML, XMLLogMessageFormatter.class);
		assignments.put(LogMessageFormat.SPEED_FORMAT, SpeedFormatter.class);
		assignments.put(LogMessageFormat.COMPETITION, CompetitionLogMessageFormatter.class);

		assignments.forEach((format, expectedFormatter) -> {
			assertThat(LogMessageFormatterFactory.createFormatter(format)).isInstanceOf(expectedFormatter);
		});
	}


	@Test
	void testInvalidFormat() {

		assertThatThrownBy(() -> LogMessageFormatterFactory.createFormatter(null))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
