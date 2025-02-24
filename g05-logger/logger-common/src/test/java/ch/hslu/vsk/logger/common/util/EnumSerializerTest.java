package ch.hslu.vsk.logger.common.util;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

enum TestEnum {
    FIRST,
    SECOND,
    THIRD
}

class EnumSerializerTest {

    @Test
    void testSerializeAndDeserializeValidEnum() throws IOException {
        EnumSerializer<TestEnum> serializer = new EnumSerializer<>(TestEnum.class);

        // Serialize a valid enum value
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        serializer.write(dataOut, TestEnum.SECOND);

        // Deserialize the serialized enum value
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        DataInputStream dataIn = new DataInputStream(byteIn);
        TestEnum deserialized = serializer.read(dataIn);

        assertEquals(TestEnum.SECOND, deserialized, "Deserialized value should match the original enum value.");
    }

    @Test
    void testSerializeAndDeserializeNullEnum() throws IOException {
        EnumSerializer<TestEnum> serializer = new EnumSerializer<>(TestEnum.class);

        // Serialize a null enum value
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        serializer.write(dataOut, null);

        // Deserialize the serialized null value
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        DataInputStream dataIn = new DataInputStream(byteIn);
        TestEnum deserialized = serializer.read(dataIn);

        assertNull(deserialized, "Deserialized value should be null for a null input.");
    }

    @Test
    void testInvalidOrdinalThrowsException() {
        EnumSerializer<TestEnum> serializer = new EnumSerializer<>(TestEnum.class);

        // Simulate a DataInputStream with an invalid ordinal
        ByteArrayInputStream byteIn = new ByteArrayInputStream(new byte[] { 0, 0, 0, 10 }); // Ordinal 10
        DataInputStream dataIn = new DataInputStream(byteIn);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> serializer.read(dataIn),
            "Should throw an exception for an invalid ordinal."
        );

        assertTrue(exception.getMessage().contains("Invalid ordinal for enum"),
                "Exception message should indicate invalid ordinal.");
    }


    @Test
    void testEnumWithAllValues() throws IOException {
        EnumSerializer<TestEnum> serializer = new EnumSerializer<>(TestEnum.class);

        for (TestEnum value : TestEnum.values()) {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(byteOut);

            // Serialize each enum value
            serializer.write(dataOut, value);

            // Deserialize and verify
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            DataInputStream dataIn = new DataInputStream(byteIn);
            TestEnum deserialized = serializer.read(dataIn);

            assertEquals(value, deserialized,
                    "Deserialized value should match the original enum value: " + value);
        }
    }
}

