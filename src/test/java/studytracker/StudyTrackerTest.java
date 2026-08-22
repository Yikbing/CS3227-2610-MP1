package studytracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import studytracker.command.CommandResult;
import studytracker.exception.StorageException;
import studytracker.exception.StudyTrackerException;
import studytracker.model.StudySession;
import studytracker.parser.Parser;
import studytracker.storage.Storage;

class StudyTrackerTest {
    private static final StudySession FIRST = new StudySession(
            "CS3227", "Testing", 45, LocalDate.of(2026, 8, 20), "Tests");

    @TempDir
    Path temporaryDirectory;

    // Verifies valid saved data is loaded and accompanied by the normal welcome message.
    @Test
    void constructor_validSavedData_loadsSessionsAndReturnsWelcomeMessage()
            throws StudyTrackerException {
        RecordingStorage storage = new RecordingStorage(temporaryDirectory, List.of(FIRST));

        StudyTracker tracker = new StudyTracker(new Parser(), storage);

        assertTrue(tracker.getStartupMessage().startsWith("Welcome to Study Tracker"));
        assertTrue(tracker.execute("list").feedback().contains("CS3227"));
    }

    // Verifies a load failure produces a warning and safely starts with an empty model.
    @Test
    void constructor_storageLoadFails_opensEmptyListAndReturnsWarning()
            throws StudyTrackerException {
        RecordingStorage storage = new RecordingStorage(temporaryDirectory, List.of());
        storage.failLoad = true;

        StudyTracker tracker = new StudyTracker(new Parser(), storage);

        assertTrue(tracker.getStartupMessage().contains("could not be loaded"));
        assertTrue(tracker.getStartupMessage().contains("simulated load failure"));
        assertTrue(tracker.execute("list").feedback().contains("No matching study sessions"));
    }

    // Verifies add, edit, and delete each cause exactly one save with the latest model state.
    @Test
    void execute_addEditDelete_savesAfterEverySuccessfulMutation() throws StudyTrackerException {
        RecordingStorage storage = new RecordingStorage(temporaryDirectory, List.of(FIRST));
        StudyTracker tracker = new StudyTracker(new Parser(), storage);

        tracker.execute("add m/CS2100 t/Assembly d/60 on/2026-08-21");
        tracker.execute("edit 1 d/90");
        tracker.execute("delete 2");

        assertEquals(3, storage.savedSnapshots.size());
        assertEquals(2, storage.savedSnapshots.get(0).size());
        assertEquals(90, storage.savedSnapshots.get(1).get(0).durationMinutes());
        assertEquals(List.of(new StudySession("CS3227", "Testing", 90,
                LocalDate.of(2026, 8, 20), "Tests")), storage.savedSnapshots.get(2));
    }

    // Verifies every read-only user command skips persistence.
    @ParameterizedTest
    @ValueSource(strings = {
        "list", "find test", "filter m/CS3227", "stats", "help", "exit"
    })
    void execute_readOnlyCommand_doesNotSave(String input) throws StudyTrackerException {
        RecordingStorage storage = new RecordingStorage(temporaryDirectory, List.of(FIRST));
        StudyTracker tracker = new StudyTracker(new Parser(), storage);

        tracker.execute(input);

        assertTrue(storage.savedSnapshots.isEmpty());
    }

    // Verifies invalid syntax neither saves data nor changes the existing model.
    @Test
    void execute_invalidInput_doesNotSaveOrChangeData() throws StudyTrackerException {
        RecordingStorage storage = new RecordingStorage(temporaryDirectory, List.of(FIRST));
        StudyTracker tracker = new StudyTracker(new Parser(), storage);

        assertThrows(StudyTrackerException.class, () -> tracker.execute("add m/CS3227"));

        assertTrue(storage.savedSnapshots.isEmpty());
        assertTrue(tracker.execute("list").feedback().contains("Testing"));
    }

    // Verifies a rejected model operation does not trigger persistence or alter data.
    @Test
    void execute_invalidModelOperation_doesNotSaveOrChangeData() throws StudyTrackerException {
        RecordingStorage storage = new RecordingStorage(temporaryDirectory, List.of(FIRST));
        StudyTracker tracker = new StudyTracker(new Parser(), storage);

        assertThrows(StudyTrackerException.class, () -> tracker.execute("delete 2"));

        assertTrue(storage.savedSnapshots.isEmpty());
        assertTrue(tracker.execute("list").feedback().contains("Testing"));
    }

    // Verifies persistence failures are surfaced to the caller as storage exceptions.
    @Test
    void execute_storageSaveFails_propagatesStorageException() {
        RecordingStorage storage = new RecordingStorage(temporaryDirectory, List.of());
        storage.failSave = true;
        StudyTracker tracker = new StudyTracker(new Parser(), storage);

        StorageException exception = assertThrows(StorageException.class,
                () -> tracker.execute("add m/CS3227 t/Testing d/45 on/2026-08-20"));

        assertTrue(exception.getMessage().contains("simulated save failure"));
    }

    // Verifies a mutation written to a real temporary file is available to a new coordinator instance.
    @Test
    void execute_mutationThenReload_preservesSavedState() throws StudyTrackerException {
        Path dataPath = temporaryDirectory.resolve("sessions.txt");
        StudyTracker firstRun = new StudyTracker(new Parser(), new Storage(dataPath));

        firstRun.execute("add m/CS3227 t/Testing d/45 on/2026-08-20 n/Tests");
        StudyTracker secondRun = new StudyTracker(new Parser(), new Storage(dataPath));
        CommandResult listResult = secondRun.execute("list");

        assertTrue(listResult.feedback().contains("CS3227"));
        assertTrue(listResult.feedback().contains("Testing"));
        assertFalse(listResult.dataChanged());
    }

    /** Records storage interactions so coordinator save decisions can be asserted deterministically. */
    private static final class RecordingStorage extends Storage {
        private final List<StudySession> initialSessions;
        private final List<List<StudySession>> savedSnapshots = new ArrayList<>();
        private boolean failLoad;
        private boolean failSave;

        private RecordingStorage(Path temporaryDirectory, List<StudySession> initialSessions) {
            super(temporaryDirectory.resolve("unused.txt"));
            this.initialSessions = List.copyOf(initialSessions);
        }

        @Override
        public List<StudySession> load() throws StorageException {
            if (failLoad) {
                throw new StorageException("simulated load failure");
            }
            return initialSessions;
        }

        @Override
        public void save(List<StudySession> sessions) throws StorageException {
            if (failSave) {
                throw new StorageException("simulated save failure");
            }
            savedSnapshots.add(List.copyOf(sessions));
        }
    }
}
