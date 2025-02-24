package ch.hslu.vsk.logger.common.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * This class can be used to send {@code NetworkTransferable} objects over an {@link OutputStream}.
 * It extends {@link DataOutputStream} to provide the functionality of serializing and writing network 
 * transferable objects to the output stream.
 */
public class NetworkMessageOutputStream extends DataOutputStream {

    /**
     * Creates a new {@code NetworkMessageOutputStream} for the given {@link OutputStream}.
     * This constructor initializes the stream with the provided output stream, allowing the writing of 
     * serialized {@code NetworkTransferable} objects to the underlying stream.
     * 
     * @param outputStream The {@link OutputStream} to which the data will be written.
     */
    public NetworkMessageOutputStream(final OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * Writes a {@code NetworkTransferable} object to the output stream.
     * This method serializes the object and sends it through the output stream.
     * After writing, the stream is flushed to ensure the data is sent immediately.
     * 
     * @param object The {@code NetworkTransferable} object to write to the output stream.
     * @throws IOException If the object could not be written to the output stream, or if there are 
     *                     issues flushing the data.
     */
    public void writeObject(final NetworkTransferable object) throws IOException {
        // Serialize the object and send it
        object.serialize(this);

        // Flush the output stream to ensure all data is written
        this.flush();
    }
}
