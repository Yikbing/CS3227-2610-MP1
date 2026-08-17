package studytracker.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import studytracker.exception.StorageException;
import studytracker.model.StudySession;

class StorageTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void saveThenLoad_specialCharacters_roundTripsExactly() throws StorageException {
        Storage storage = new Storage(temporaryDirectory.resolve("nested").resolve("sessions.txt"));
        StudySession session = new StudySession("CS3227", "Parsing | storage", 75,
                LocalDate.of(2026, 8, 17), "Tabs\tand Unicode: 学习");

        storage.save(List.of(session));
        List<StudySession> loaded = storage.load();

        assertEquals(List.of(session), loaded);
    }

    @Test
    void load_missingFile_returnsEmptyList() throws StorageException {
        Storage storage = new Storage(temporaryDirectory.resolve("missing.txt"));
        assertTrue(storage.load().isEmpty());
    }
}
