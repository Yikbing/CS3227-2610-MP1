package studytracker.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import studytracker.exception.StudyTrackerException;
import studytracker.model.EditDescriptor;
import studytracker.model.SessionList;
import studytracker.model.StudySession;

class CommandTest {
    private static final StudySession FIRST = new StudySession(
            "CS3227", "Testing", 45, LocalDate.of(2026, 8, 20), "Tests");
    private static final StudySession SECOND = new StudySession(
            "CS2100", "Assembly", 60, LocalDate.of(2026, 8, 21), "");

    // Verifies add execution changes the model and returns mutation feedback and flags.
    @Test
    void addCommand_execute_addsSessionAndReturnsChangedResult() {
        SessionList sessions = new SessionList();

        CommandResult result = new AddCommand(FIRST).execute(sessions);

        assertEquals(List.of(FIRST), sessions.asList());
        assertTrue(result.feedback().contains("Recorded study session"));
        assertChanged(result);
    }

    // Verifies edit execution updates the selected session and reports the changed value.
    @Test
    void editCommand_execute_updatesSessionAndReturnsChangedResult() throws StudyTrackerException {
        SessionList sessions = new SessionList(List.of(FIRST));
        EditDescriptor descriptor = new EditDescriptor(Optional.empty(), Optional.of("Storage"),
                Optional.empty(), Optional.empty(), Optional.empty());

        CommandResult result = new EditCommand(1, descriptor).execute(sessions);

        assertEquals("Storage", sessions.asList().get(0).topic());
        assertTrue(result.feedback().contains("Updated study session"));
        assertTrue(result.feedback().contains("Storage"));
        assertChanged(result);
    }

    // Verifies an invalid edit propagates the model error without changing existing data.
    @Test
    void editCommand_invalidIndex_throwsAndPreservesSessions() {
        SessionList sessions = new SessionList(List.of(FIRST));
        EditDescriptor descriptor = new EditDescriptor(Optional.empty(), Optional.of("Storage"),
                Optional.empty(), Optional.empty(), Optional.empty());

        assertThrows(StudyTrackerException.class, () -> new EditCommand(2, descriptor).execute(sessions));
        assertEquals(List.of(FIRST), sessions.asList());
    }

    // Verifies delete execution removes the selected session and reports the deleted value.
    @Test
    void deleteCommand_execute_removesSessionAndReturnsChangedResult() throws StudyTrackerException {
        SessionList sessions = new SessionList(List.of(FIRST, SECOND));

        CommandResult result = new DeleteCommand(1).execute(sessions);

        assertEquals(List.of(SECOND), sessions.asList());
        assertTrue(result.feedback().contains("Deleted study session"));
        assertTrue(result.feedback().contains("CS3227"));
        assertChanged(result);
    }

    // Verifies an invalid deletion propagates the model error without changing existing data.
    @Test
    void deleteCommand_invalidIndex_throwsAndPreservesSessions() {
        SessionList sessions = new SessionList(List.of(FIRST));

        assertThrows(StudyTrackerException.class, () -> new DeleteCommand(2).execute(sessions));
        assertEquals(List.of(FIRST), sessions.asList());
    }

    // Verifies list output uses its heading, one-based numbering, and insertion order.
    @Test
    void listCommand_populatedList_formatsNumberedReadOnlyResult() {
        CommandResult result = new ListCommand().execute(new SessionList(List.of(FIRST, SECOND)));

        assertTrue(result.feedback().startsWith("Study history:"));
        assertTrue(result.feedback().contains("\n1. "));
        assertTrue(result.feedback().contains("\n2. "));
        assertTrue(result.feedback().indexOf("CS3227") < result.feedback().indexOf("CS2100"));
        assertReadOnly(result);
    }

    // Verifies list output gives a clear message when no sessions exist.
    @Test
    void listCommand_emptyList_reportsNoMatches() {
        CommandResult result = new ListCommand().execute(new SessionList());

        assertTrue(result.feedback().contains("No matching study sessions."));
        assertReadOnly(result);
    }

    // Verifies find output contains its heading, matching session, numbering, and read-only flags.
    @Test
    void findCommand_matchingSession_formatsReadOnlyResult() {
        CommandResult result = new FindCommand("test").execute(new SessionList(List.of(FIRST, SECOND)));

        assertTrue(result.feedback().startsWith("Sessions matching 'test':"));
        assertTrue(result.feedback().contains("\n1. "));
        assertTrue(result.feedback().contains("CS3227"));
        assertFalse(result.feedback().contains("CS2100"));
        assertReadOnly(result);
    }

    // Verifies find output reports an empty result without changing data.
    @Test
    void findCommand_noMatch_reportsNoMatches() {
        CommandResult result = new FindCommand("calculus").execute(new SessionList(List.of(FIRST)));

        assertTrue(result.feedback().contains("No matching study sessions."));
        assertReadOnly(result);
    }

    // Verifies filter execution presents representative filtered data as a read-only result.
    @Test
    void filterCommand_matchingSession_formatsReadOnlyResult() {
        FilterCommand command = new FilterCommand("CS2100", null, null);

        CommandResult result = command.execute(new SessionList(List.of(FIRST, SECOND)));

        assertTrue(result.feedback().startsWith("Filtered study sessions:"));
        assertTrue(result.feedback().contains("CS2100"));
        assertFalse(result.feedback().contains("CS3227"));
        assertReadOnly(result);
    }

    // Verifies populated statistics are formatted and marked read-only.
    @Test
    void statsCommand_populatedList_returnsFormattedReadOnlyResult() {
        CommandResult result = new StatsCommand().execute(new SessionList(List.of(FIRST, SECOND)));

        assertTrue(result.feedback().contains("Sessions: 2"));
        assertTrue(result.feedback().contains("Total time: 1 h 45 min"));
        assertReadOnly(result);
    }

    // Verifies empty statistics use the dedicated empty-history message.
    @Test
    void statsCommand_emptyList_returnsEmptyReadOnlyResult() {
        CommandResult result = new StatsCommand().execute(new SessionList());

        assertEquals("No study sessions have been recorded yet.", result.feedback());
        assertReadOnly(result);
    }

    // Verifies help presents every documented command format without changing state.
    @Test
    void helpCommand_execute_listsAllCommandsAsReadOnlyResult() {
        CommandResult result = new HelpCommand().execute(new SessionList());

        for (String command : List.of("add ", "list", "edit ", "delete ", "find ",
                "filter ", "stats", "help", "exit")) {
            assertTrue(result.feedback().contains(command));
        }
        assertReadOnly(result);
    }

    // Verifies exit requests application closure without claiming that data changed.
    @Test
    void exitCommand_execute_returnsExitResult() {
        CommandResult result = new ExitCommand().execute(new SessionList());

        assertTrue(result.feedback().contains("Goodbye"));
        assertFalse(result.dataChanged());
        assertTrue(result.exit());
    }

    private static void assertChanged(CommandResult result) {
        assertTrue(result.dataChanged());
        assertFalse(result.exit());
    }

    private static void assertReadOnly(CommandResult result) {
        assertFalse(result.dataChanged());
        assertFalse(result.exit());
    }
}
