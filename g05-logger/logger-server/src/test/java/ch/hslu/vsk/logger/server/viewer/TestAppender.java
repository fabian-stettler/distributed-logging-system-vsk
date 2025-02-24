package ch.hslu.vsk.logger.server.viewer;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class TestAppender extends AbstractAppender {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    protected TestAppender(String name, Layout<? extends Serializable> layout) {
        super(name, null, layout, false, null);
    }

    /**
     * append gets called in the log4j2 framework when a log event is logged.
     * @param event The LogEvent.
     */
    @Override
    public void append(LogEvent event) {
        try {
            outputStream.write(getLayout().toByteArray(event));
        } catch (Exception e) {
            // Handle exception
        }
    }

    public void clear() {
        outputStream.reset();
    }

    /**
     * Get the output of the appender.
     * @return The output as a string.
     */
    public String getOutput() {
        return outputStream.toString();
    }

    public static TestAppender createAppender() {
        Layout<? extends Serializable> layout = PatternLayout.newBuilder().withPattern("%m%n").build();
        return new TestAppender("TestAppender", layout);
    }
}