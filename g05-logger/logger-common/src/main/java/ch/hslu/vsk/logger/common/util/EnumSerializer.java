package ch.hslu.vsk.logger.common.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A utility class for serializing and deserializing enum values in a compact and efficient manner.
 * The class leverages the ordinal value of enum constants for serialization, which minimizes the data size.
 *
 * @param <T> The type of the enum being serialized/deserialized. Must extend {@link Enum}.
 */
public class EnumSerializer<T extends Enum<?>> {

    /** Array of enum constants for the specified type. */
    private final T[] enumConstants;

    /**
     * Constructs an {@code EnumSerializer} for the specified enum type.
     *
     * @param type the {@code Class} of the enum type to be serialized/deserialized.
     * @throws IllegalArgumentException if the provided type is not an enum.
     */
    public EnumSerializer(Class<T> type) {
        if (!type.isEnum()) {
            throw new IllegalArgumentException("The type must be an enum: " + type);
        }
        this.enumConstants = type.getEnumConstants();
    }

    /**
     * Serializes the specified enum value to a {@link DataOutputStream}.
     *
     * @param output the {@code DataOutputStream} to write the serialized data to.
     * @param object the enum value to serialize, or {@code null}.
     * @throws IOException if an I/O error occurs while writing to the stream.
     */
    public void write(DataOutputStream output, T object) throws IOException {
        if (object == null) {
            output.writeInt(-1); // Write -1 to represent null
        } else {
            output.writeInt(object.ordinal()); // Write the ordinal value of the enum constant
        }
    }

    /**
     * Deserializes an enum value from a {@link DataInputStream}.
     *
     * @param input the {@code DataInputStream} to read the serialized data from.
     * @return the deserialized enum value, or {@code null} if the input indicates a null value.
     * @throws IOException              if an I/O error occurs while reading from the stream.
     * @throws IllegalArgumentException if the read ordinal value is invalid.
     */
    public T read(DataInputStream input) throws IOException {
        int ordinal = input.readInt(); // Read the ordinal value
        if (ordinal == -1) {
            return null; // Return null if ordinal is -1
        }
        if (ordinal < 0 || ordinal >= enumConstants.length) {
            throw new IllegalArgumentException(
                "Invalid ordinal for enum: " + ordinal + ". Must be between 0 and " + (enumConstants.length - 1)
            );
        }
        return enumConstants[ordinal]; // Return the corresponding enum constant
    }
}
