package ch.hslu.vsk.logger.common.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.hslu.vsk.logger.common.LogMessage;


/**
 * This class is used to receive {@code NetworkTransferable} objects from an {@link InputStream}.
 * It extends {@link DataInputStream} to provide methods for reading objects that implement the 
 * {@code NetworkTransferable} interface.
 */
public class NetworkMessageInputStream extends DataInputStream {

    /**
     * Constructs a new {@code NetworkMessageInputStream} using the provided {@link InputStream}.
     * This enables reading network messages as objects from the stream.
     *
     * @param inputStream The underlying input stream to read data from.
     */
    public NetworkMessageInputStream(final InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Reads the next {@code NetworkTransferable} object from the input stream.
     * This method reads the serial version identifier to determine the type of object
     * and deserializes it accordingly.
     *
     * @return The deserialized {@code NetworkTransferable} object.
     * @throws IOException If an I/O error occurs while reading from the input stream.
     * @throws IllegalArgumentException If the serial version identifier does not match any known type.
     */
    public NetworkTransferable nextObject() throws IOException {

        // Read the serial version ID from the stream
        int serialVersionId = this.readShort();

        // Check if the serialVersionId matches the expected type
        if (serialVersionId == LogMessage.serialVersionUID) {
            return LogMessage.deserialize(this);
        }

        // Return null if no matching type is found
        return null;
    }

}
