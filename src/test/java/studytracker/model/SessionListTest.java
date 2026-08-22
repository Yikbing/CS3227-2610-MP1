package studytracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import studytracker.exception.StudyTrackerException;

class SessionListTest {
    private static final StudySession SESSION = new StudySession(
            "CS3227", "Command parsing", 90, LocalDate.of(2026, 8, 17), "Parser tests");
    private static final StudySession SECOND_SESSION = new StudySession(
            "CS2100", "Assembly", 60, LocalDate.of(2026, 8, 19), "Tutorial");

    // Verifies that added sessions remain in insertion order.
    @Test
    void add_multipleSessions_preservesInsertionOrder() {
        SessionList sessions = new SessionList();

        sessions.add(SESSION);
        sessions.add(SECOND_SESSION);

        assertEquals(List.of(SESSION, SECOND_SESSION), sessions.asList());
    }

    // Verifies that an edit changes both the returned value and the session stored in the list.
    @Test
    void edit_onlySuppliedField_changesStoredSession() throws StudyTrackerException {
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
        assertEquals(List.of(result), sessions.asList());
    }

    // Verifies the first and last valid one-based indexes used by the user interface.
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void edit_boundaryValidIndex_changesRequestedSession(int index) throws StudyTrackerException {
        SessionList sessions = new SessionList(List.of(SESSION, SECOND_SESSION));
        EditDescriptor edit = new EditDescriptor(Optional.empty(), Optional.empty(),
                Optional.of(30), Optional.empty(), Optional.empty());

        StudySession result = sessions.edit(index, edit);

        assertEquals(30, result.durationMinutes());
        assertEquals(result, sessions.asList().get(index - 1));
    }

    // Verifies that invalid indexes are rejected and cannot alter existing data.
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 3})
    void edit_invalidIndex_throwsExceptionAndPreservesList(int index) {
        SessionList sessions = new SessionList(List.of(SESSION, SECOND_SESSION));
        List<StudySession> original = sessions.asList();
        EditDescriptor edit = new EditDescriptor(Optional.empty(), Optional.of("Storage"),
                Optional.empty(), Optional.empty(), Optional.empty());

        StudyTrackerException exception = assertThrows(
                StudyTrackerException.class, () -> sessions.edit(index, edit));

        assertTrue(exception.getMessage().contains("between 1 and 2"));
        assertEquals(original, sessions.asList());
    }

    // Verifies that deletion returns the selected session and removes only that session.
    @Test
    void delete_validIndex_removesAndReturnsCorrectSession() throws StudyTrackerException {
        SessionList sessions = new SessionList(List.of(SESSION, SECOND_SESSION));

        StudySession deleted = sessions.delete(1);

        assertEquals(SESSION, deleted);
        assertEquals(List.of(SECOND_SESSION), sessions.asList());
    }

    // Verifies deletion also accepts the last valid one-based index.
    @Test
    void delete_lastValidIndex_removesLastSession() throws StudyTrackerException {
        SessionList sessions = new SessionList(List.of(SESSION, SECOND_SESSION));

        StudySession deleted = sessions.delete(2);

        assertEquals(SECOND_SESSION, deleted);
        assertEquals(List.of(SESSION), sessions.asList());
    }

    // Verifies both invalid-index partitions without repeating equivalent test setup.
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 3})
    void delete_invalidIndex_throwsExceptionAndPreservesList(int index) {
        SessionList sessions = new SessionList(List.of(SESSION, SECOND_SESSION));
        List<StudySession> original = sessions.asList();

        assertThrows(StudyTrackerException.class, () -> sessions.delete(index));

        assertEquals(original, sessions.asList());
    }

    // Verifies case-insensitive substring matching across module, topic, and notes.
    @Test
    void find_keywordInDifferentFields_returnsMatchesInInsertionOrder() {
        StudySession moduleMatch = new StudySession(
                "PARSE101", "Basics", 30, LocalDate.of(2026, 8, 16), "");
        StudySession topicMatch = SESSION;
        StudySession notesMatch = new StudySession(
                "CS2100", "Assembly", 60, LocalDate.of(2026, 8, 18), "parse practice");
        SessionList sessions = new SessionList(List.of(moduleMatch, topicMatch, notesMatch, SECOND_SESSION));

        List<StudySession> result = sessions.find("PaRsE");

        assertEquals(List.of(moduleMatch, topicMatch, notesMatch), result);
    }

    // Verifies a valid search with no matching field returns an empty result.
    @Test
    void find_noMatchingSession_returnsEmptyList() {
        SessionList sessions = new SessionList(List.of(SESSION, SECOND_SESSION));

        assertTrue(sessions.find("calculus").isEmpty());
    }

    // Verifies combined filters, inclusive date boundaries, case-insensitive modules, and newest-first order.
    @Test
    void filter_combinedCriteria_includesBoundariesAndSortsNewestFirst() {
        StudySession lowerBoundary = new StudySession(
                "CS3227", "Lower", 30, LocalDate.of(2026, 8, 1), "");
        StudySession middle = new StudySession(
                "cs3227", "Middle", 30, LocalDate.of(2026, 8, 15), "");
        StudySession upperBoundary = new StudySession(
                "CS3227", "Upper", 30, LocalDate.of(2026, 8, 31), "");
        StudySession wrongModule = new StudySession(
                "CS2100", "Other", 30, LocalDate.of(2026, 8, 20), "");
        StudySession beforeRange = new StudySession(
                "CS3227", "Before", 30, LocalDate.of(2026, 7, 31), "");
        StudySession afterRange = new StudySession(
                "CS3227", "After", 30, LocalDate.of(2026, 9, 1), "");
        SessionList sessions = new SessionList(
                List.of(lowerBoundary, middle, upperBoundary, wrongModule, beforeRange, afterRange));

        List<StudySession> result = sessions.filter(
                "Cs3227", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertEquals(List.of(upperBoundary, middle, lowerBoundary), result);
    }

    // Verifies each optional filter criterion works independently when the others are absent.
    @ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("singleFilterCases")
    void filter_singleCriterion_appliesOnlySuppliedCriterion(
            String module, LocalDate from, LocalDate to, List<StudySession> expected) {
        SessionList sessions = new SessionList(List.of(SESSION, SECOND_SESSION));

        assertEquals(expected, sessions.filter(module, from, to));
    }

    // Verifies statistics totals and the documented case-insensitive module grouping.
    @Test
    void calculateStatistics_groupsMinutesByModuleCaseInsensitively() {
        SessionList sessions = new SessionList();
        sessions.add(SESSION);
        sessions.add(new StudySession("cs3227", "Storage", 30,
                LocalDate.of(2026, 8, 18), ""));
        sessions.add(SECOND_SESSION);

        StudyStatistics result = sessions.calculateStatistics();

        assertEquals(3, result.sessionCount());
        assertEquals(180, result.totalMinutes());
        assertEquals(120, result.minutesByModule().get("CS3227"));
        assertEquals(60, result.minutesByModule().get("CS2100"));
        assertEquals(2, result.minutesByModule().size());
    }

    // Verifies the neutral statistics result for an empty session list.
    @Test
    void calculateStatistics_emptyList_returnsZeroStatistics() {
        StudyStatistics result = new SessionList().calculateStatistics();

        assertEquals(0, result.sessionCount());
        assertEquals(0, result.totalMinutes());
        assertTrue(result.minutesByModule().isEmpty());
    }

    // Verifies that callers cannot use the public list view to mutate internal state.
    @Test
    void asList_attemptedModification_isRejectedAndPreservesList() {
        SessionList sessions = new SessionList(List.of(SESSION));
        List<StudySession> view = sessions.asList();

        assertThrows(UnsupportedOperationException.class, () -> view.add(SECOND_SESSION));
        assertEquals(List.of(SESSION), sessions.asList());
    }

    private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> singleFilterCases() {
        return java.util.stream.Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        "cs3227", null, null, List.of(SESSION)),
                org.junit.jupiter.params.provider.Arguments.of(
                        null, LocalDate.of(2026, 8, 18), null, List.of(SECOND_SESSION)),
                org.junit.jupiter.params.provider.Arguments.of(
                        null, null, LocalDate.of(2026, 8, 18), List.of(SESSION)));
    }
}
