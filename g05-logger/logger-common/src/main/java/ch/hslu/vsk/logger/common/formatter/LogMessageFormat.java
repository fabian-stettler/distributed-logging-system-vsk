package ch.hslu.vsk.logger.common.formatter;

public enum LogMessageFormat {

    /**
     * JSON format:
     * A structured, machine-readable format that is widely used for data interchange.
     * Useful for integration with external systems or APIs.
     */
    JSON,

    /**
     * HumanReadable format:
     * A format designed for ease of reading by humans.
     * Typically used for debugging or logging purposes in environments where log files are manually inspected.
     */
    HUMAN_READABLE,

    /**
     * SpeedFormat:
     * A format optimized for performance, focusing on fast serialization and deserialization.
     * Suitable for scenarios where processing speed is critical.
     */
    SPEED_FORMAT,

    /**
     * Competition format:
	 * A format used for the competition at the end of the the vsk module.
     */
	COMPETITION,

    /**
     * XML format:
     * A structured, verbose format commonly used in legacy systems or where strict schema validation is required.
     */
    XML
}
