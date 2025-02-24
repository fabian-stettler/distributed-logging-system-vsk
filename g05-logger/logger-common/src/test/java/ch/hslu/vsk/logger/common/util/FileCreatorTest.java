package ch.hslu.vsk.logger.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link FileCreator} class.
 */
class FileCreatorTest {

    private Path tempTestDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary directory for testing
        tempTestDir = Files.createTempDirectory("fileCreatorTest");
        System.out.println("Test directory created at: " + tempTestDir);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Recursively delete the test directory and its contents
        Files.walk(tempTestDir)
                .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete files before directories
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete: " + path);
                    }
                });

        // Delete the temporary test directory itself
        if (Files.exists(tempTestDir)) {
            Files.delete(tempTestDir);
            System.out.println("Test directory deleted: " + tempTestDir);
        } else {
            System.out.println("Test directory already deleted.");
        }
    }

    @Test
    void testCreateFileInExistingDirectory() throws IOException {
        Path existingDir = tempTestDir.resolve("existingDir");
        Files.createDirectories(existingDir); // Pre-create the directory

        Path testFilePath = existingDir.resolve("testfile.txt");

        // Test file creation in an existing directory
        boolean created = FileCreator.createFile(testFilePath);
        assertTrue(created, "File should be created in an existing directory.");
        assertTrue(Files.exists(testFilePath), "File should exist after creation.");
    }

    @Test
    void testCreateFileWithNoParentDirectory() {
        Path testFilePath = tempTestDir.resolve("testfile.txt");

        // Test file creation at the root of the temp directory
        boolean created = FileCreator.createFile(testFilePath);
        assertTrue(created, "File should be created if parent directory exists.");
        assertTrue(Files.exists(testFilePath), "File should exist after creation.");
    }
}
