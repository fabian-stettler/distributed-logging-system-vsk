package ch.hslu.vsk.logger.common.formatter;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.common.LogMessage;
import ch.hslu.vsk.logger.common.util.InstantSerializer;

/**
 * This class formats {@code LogMessage} objects into XML format and parses XML-formatted strings back into
 * {@code LogMessage} objects.
 */
public class XMLLogMessageFormatter implements LogMessageFormatter {

	/**
	 * Creates a new formatter that can convert {@link LogMessage}s to XML and convert them back
	 */
	public XMLLogMessageFormatter() {
		// No initialization is necessary for this formatter.
	}

    /**
     * Converts a {@code LogMessage} object into a string in XML format.
     * The fields of the log message, including receivedAt, timestamp, clientName, logLevel, and message,
     * are formatted into an XML structure. Special characters in the message are escaped to ensure valid XML.
     * 
     * @param message The {@code LogMessage} object to format into XML.
     * @return The formatted log message as a string in XML format.
     */
    @Override
    public String serialize(final LogMessage message) {
        String receivedAtFormatted = InstantSerializer.stringifyInstant(message.getReceivedAtServer());
        String timestampFormatted = InstantSerializer.stringifyInstant(message.getTimestamp());

        // Escape special characters in the message to ensure valid XML
        String messageString = message.getMessage().replace("<", "&lt;").replace(">", "&gt;");

        // Generate XML string representation of the log message
        return "<logMessage>" +
               "<receivedAt>" + receivedAtFormatted + "</receivedAt>" +
               "<timestamp>" + timestampFormatted + "</timestamp>" +
               "<clientName>" + message.getClientName() + "</clientName>" +
               "<logLevel>" + message.getLogLevel() + "</logLevel>" +
               "<message>" + messageString + "</message>" +
               "</logMessage>";
    }

    /**
     * Converts an XML formatted string into a {@code LogMessage} object.
     * This method checks that the XML string contains all required attributes: receivedAt, timestamp, clientName,
     * logLevel, and message. If any attribute is missing or the format is invalid, an exception is thrown.
     * 
     * @param message The XML formatted log message to parse.
     * @return The parsed {@code LogMessage} object.
     * @throws IllegalArgumentException If the XML string does not contain the required attributes or if the format is invalid.
     * @throws DateTimeParseException If there is an error parsing the timestamp or receivedAt date values.
     */
    @Override
    public LogMessage parse(final String message) {
        // Check that the message contains all necessary attributes
        if (!checkIfContainsAttribute(message, "receivedAt")) {
            throw new IllegalArgumentException("The xml string does not contain a receivedAt attribute.");
        }
        if (!checkIfContainsAttribute(message, "timestamp")) {
            throw new IllegalArgumentException("The xml string does not contain a timestamp attribute.");
        }
        if (!checkIfContainsAttribute(message, "clientName")) {
            throw new IllegalArgumentException("The xml string does not contain a clientName attribute.");
        }
        if (!checkIfContainsAttribute(message, "logLevel")) {
            throw new IllegalArgumentException("The xml string does not contain a logLevel attribute.");
        }
        if (!checkIfContainsAttribute(message, "message")) {
            throw new IllegalArgumentException("The xml string does not contain a message attribute.");
        }

        // Extract the values from the XML message
        try {
            String timestampServerString = message.substring(message.indexOf("<receivedAt>") + 12, message.indexOf("</receivedAt>"));
            String timestampString = message.substring(message.indexOf("<timestamp>") + 11, message.indexOf("</timestamp>"));
            String clientName = message.substring(message.indexOf("<clientName>") + 12, message.indexOf("</clientName>"));
            String logLevelString = message.substring(message.indexOf("<logLevel>") + 10, message.indexOf("</logLevel>"));
            String messageString = message.substring(message.indexOf("<message>") + 9, message.indexOf("</message>"));

            // Parse the timestamps
            Instant timestampServer = InstantSerializer.parseInstant(timestampServerString);
            Instant timestamp = InstantSerializer.parseInstant(timestampString);

            // Unescape the special characters in the message
            messageString = messageString.replace("&lt;", "<").replace("&gt;", ">");

            // Convert the log level string to the LogLevel enum
            LogLevel logLevel = LogLevel.valueOf(logLevelString);

            // Return the LogMessage object
            return new LogMessage(timestampServer, timestamp, clientName, logLevel, messageString);
        } catch (StringIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("Invalid format", ex);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format", ex);
        }
    }

    /**
     * Checks if the XML string contains the specified attribute.
     * 
     * @param xml The XML string to check.
     * @param attribute The name of the attribute to look for.
     * @return {@code true} if the attribute is present in the XML string, {@code false} otherwise.
     */
    private boolean checkIfContainsAttribute(String xml, String attribute) {
        return xml.contains("<" + attribute + ">") && xml.contains("</" + attribute + ">");
    }
}
