package studytracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import studytracker.exception.StudyTrackerException;

class SessionListTest {
    private static final StudySession SESSION = new StudySession(
            "CS3227", "Command parsing", 90, LocalDate.of(2026, 8, 17), "Parser tests");

    @Test
    void edit_onlySuppliedFieldChanged() throws StudyTrackerException {
        SessionList sessions = new SessionList();
        sessions.add(SESSION);
        EditDescriptor edit = new EditDescriptor(Optional.empty(), Optional.of("Storage"),
                Optional.empty(), Optional.empty(), Optional.empty());

        StudySession result = sessions.edit(1, edit);

        assertEquals("Storage", result.topic());
        assertEquals(SESSION.module(), result.module());
        assertEquals(SESSION.durationMinutes(), result.durationMinutes());
        assertEquals(SESSION.date(), result.date());
        assertEquals(SESSION.notes(), result.notes());
    }

    @Test
    void delete_invalidIndex_throwsUserFacingException() {
        SessionList sessions = new SessionList();
        assertThrows(StudyTrackerException.class, () -> sessions.delete(1));
    }

    @Test
    void calculateStatistics_groupsMinutesByModule() {
        SessionList sessions = new SessionList();
        sessions.add(SESSION);
        sessions.add(new StudySession("CS3227", "Storage", 30,
                LocalDate.of(2026, 8, 18), ""));
        sessions.add(new StudySession("CS2100", "Assembly", 60,
                LocalDate.of(2026, 8, 19), ""));

        StudyStatistics result = sessions.calculateStatistics();

        assertEquals(3, result.sessionCount());
        assertEquals(180, result.totalMinutes());
        assertEquals(120, result.minutesByModule().get("CS3227"));
        assertEquals(60, result.minutesByModule().get("CS2100"));
    }
}
