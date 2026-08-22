package studytracker.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import studytracker.exception.StorageException;
import studytracker.model.StudySession;

class StorageTest {
    private static final StudySession SESSION = new StudySession(
            "CS3227", "Parsing | storage", 75, LocalDate.of(2026, 8, 17),
            "Tabs\tand Unicode: 学习");
    private static final StudySession SECOND_SESSION = new StudySession(
            "CS2100", "Assembly", 60, LocalDate.of(2026, 8, 18), "Tutorial");

    @TempDir
    Path temporaryDirectory;

    // Verifies nested-directory creation and exact preservation of separators, tabs, and genuine Unicode.
    @Test
    void saveThenLoad_specialCharacters_roundTripsExactly() throws StorageException {
        Storage storage = new Storage(temporaryDirectory.resolve("nested").resolve("sessions.txt"));

        storage.save(List.of(SESSION));
        List<StudySession> loaded = storage.load();

        assertEquals(List.of(SESSION), loaded);
    }

    // Verifies multiple records retain their original order across persistence.
    @Test
    void saveThenLoad_multipleSessions_preservesOrder() throws StorageException {
        Storage storage = new Storage(temporaryDirectory.resolve("sessions.txt"));

        storage.save(List.of(SESSION, SECOND_SESSION));

        assertEquals(List.of(SESSION, SECOND_SESSION), storage.load());
    }

    // Verifies saving an empty collection produces an empty collection when reloaded.
    @Test
    void saveThenLoad_emptyList_returnsEmptyList() throws StorageException {
        Storage storage = new Storage(temporaryDirectory.resolve("sessions.txt"));

        storage.save(List.of());

        assertTrue(storage.load().isEmpty());
    }

    // Verifies a later save replaces old data instead of appending to it.
    @Test
    void save_existingFile_replacesPreviousContents() throws StorageException {
        Storage storage = new Storage(temporaryDirectory.resolve("sessions.txt"));
        storage.save(List.of(SESSION, SECOND_SESSION));

        storage.save(List.of(SECOND_SESSION));

        assertEquals(List.of(SECOND_SESSION), storage.load());
    }

    // Verifies first-run behaviour when no storage file has been created yet.
    @Test
    void load_missingFile_returnsEmptyList() throws StorageException {
        Storage storage = new Storage(temporaryDirectory.resolve("missing.txt"));

        assertTrue(storage.load().isEmpty());
    }

    // Verifies blank records are ignored without changing the order of valid records.
    @Test
    void load_fileContainingBlankLines_ignoresBlankLines() throws IOException, StorageException {
        Path dataPath = temporaryDirectory.resolve("sessions.txt");
        SessionFileCodec codec = new SessionFileCodec();
        Files.write(dataPath, List.of(codec.encode(SESSION), "", "   ", codec.encode(SECOND_SESSION)),
                StandardCharsets.UTF_8);

        assertEquals(List.of(SESSION, SECOND_SESSION), new Storage(dataPath).load());
    }

    // Verifies corrupt persisted data is reported with its line number.
    @Test
    void load_malformedRecord_throwsStorageException() throws IOException {
        Path dataPath = temporaryDirectory.resolve("sessions.txt");
        Files.writeString(dataPath, "not-a-valid-record", StandardCharsets.UTF_8);

        StorageException exception = assertThrows(
                StorageException.class, () -> new Storage(dataPath).load());

        assertTrue(exception.getMessage().contains("line 1"));
    }

    // Verifies corruption after a valid record reports its actual file line number.
    @Test
    void load_malformedSecondRecord_reportsCorrectLineNumber() throws IOException {
        Path dataPath = temporaryDirectory.resolve("sessions.txt");
        SessionFileCodec codec = new SessionFileCodec();
        Files.write(dataPath, List.of(codec.encode(SESSION), "not-a-valid-record"),
                StandardCharsets.UTF_8);

        StorageException exception = assertThrows(
                StorageException.class, () -> new Storage(dataPath).load());

        assertTrue(exception.getMessage().contains("line 2"));
    }

    // Verifies loading a directory wraps the filesystem failure in the application's storage exception.
    @Test
    void load_directoryPath_wrapsReadFailureInStorageException() {
        Storage storage = new Storage(temporaryDirectory);

        StorageException exception = assertThrows(StorageException.class, storage::load);

        assertTrue(exception.getMessage().contains("Could not read saved sessions"));
    }

    // Verifies a stable write failure is wrapped when the intended parent is an existing file.
    @Test
    void save_parentPathIsAFile_wrapsWriteFailureInStorageException() throws IOException {
        Path parentFile = temporaryDirectory.resolve("not-a-directory");
        Files.writeString(parentFile, "content", StandardCharsets.UTF_8);
        Storage storage = new Storage(parentFile.resolve("sessions.txt"));

        StorageException exception = assertThrows(
                StorageException.class, () -> storage.save(List.of(SESSION)));

        assertTrue(exception.getMessage().contains("Could not save study sessions"));
    }
}
