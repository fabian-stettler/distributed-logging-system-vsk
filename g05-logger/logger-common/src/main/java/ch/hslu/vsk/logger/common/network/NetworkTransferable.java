package ch.hslu.vsk.logger.common.network;

import java.io.IOException;


/**
 * This interface defines the contract for objects that can be serialized and deserialized
 * for network transfer. Implementing classes must define how to convert their state to and
 * from a data stream.
 */
public interface NetworkTransferable {

    /**
     * Deserializes an object from the provided input stream and returns a new instance.
     * <p>
     * This method reads data from the input stream and reconstructs the object in memory.
     * </p>
     *
     * @param inputStream The input stream from which the object data will be read.
     * @return A new instance of the object.
     * @throws IOException If an I/O error occurs during deserialization.
     */
    static NetworkTransferable deserialize(NetworkMessageInputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("deserialize() method must be implemented by concrete class.");
    }

    /**
     * Serializes the object to the provided output stream.
     * <p>
     * This method writes the object's state to the output stream.
     * </p>
     *
     * @param outputStream The output stream to which the object data will be written.
     * @throws IOException If an I/O error occurs during serialization.
     */
    void serialize(NetworkMessageOutputStream outputStream) throws IOException;
}
