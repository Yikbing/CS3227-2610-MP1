package studytracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class StudySessionTest {
    private static final LocalDate DATE = LocalDate.of(2026, 8, 17);

    // Verifies that valid text is trimmed and null notes are normalised to an empty value.
    @Test
    void constructor_validInput_normalisesTextAndNullNotes() {
        StudySession session = new StudySession("  CS3227  ", "  Testing  ", 30, DATE, null);

        assertEquals("CS3227", session.module());
        assertEquals("Testing", session.topic());
        assertEquals("", session.notes());
    }

    // Verifies that every required text field rejects both null and blank values.
    @ParameterizedTest
    @MethodSource("invalidRequiredTextCases")
    void constructor_invalidRequiredText_throwsIllegalArgumentException(String module, String topic) {
        assertThrows(IllegalArgumentException.class,
                () -> new StudySession(module, topic, 30, DATE, ""));
    }

    // Verifies the duration boundary by rejecting zero and negative minutes.
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void constructor_nonPositiveDuration_throwsIllegalArgumentException(int duration) {
        assertThrows(IllegalArgumentException.class,
                () -> new StudySession("CS3227", "Testing", duration, DATE, ""));
    }

    // Verifies that a study session cannot exist without a date.
    @Test
    void constructor_nullDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new StudySession("CS3227", "Testing", 30, null, ""));
    }

    // Verifies minute, exact-hour, and mixed-hour duration formatting boundaries.
    @ParameterizedTest
    @MethodSource("durationFormattingCases")
    void formatDuration_boundaryValues_returnsExpectedText(int minutes, String expected) {
        assertEquals(expected, StudySession.formatDuration(minutes));
    }

    // Verifies display output includes core fields and includes notes only when supplied.
    @ParameterizedTest
    @MethodSource("displayCases")
    void toDisplayString_withAndWithoutNotes_formatsExpectedText(String notes, boolean notesExpected) {
        StudySession session = new StudySession("CS3227", "Testing", 90, DATE, notes);

        String display = session.toDisplayString();

        assertTrue(display.contains("CS3227"));
        assertTrue(display.contains("Testing"));
        assertTrue(display.contains("1 h 30 min"));
        assertTrue(display.contains("17 Aug 2026"));
        assertEquals(notesExpected, display.contains("Review notes"));
    }

    private static Stream<Arguments> invalidRequiredTextCases() {
        return Stream.of(
                Arguments.of(null, "Testing"),
                Arguments.of("   ", "Testing"),
                Arguments.of("CS3227", null),
                Arguments.of("CS3227", "   "));
    }

    private static Stream<Arguments> durationFormattingCases() {
        return Stream.of(
                Arguments.of(1, "1 min"), Arguments.of(59, "59 min"),
                Arguments.of(60, "1 h"), Arguments.of(61, "1 h 1 min"),
                Arguments.of(90, "1 h 30 min"), Arguments.of(120, "2 h"));
    }

    private static Stream<Arguments> displayCases() {
        return Stream.of(Arguments.of("", false), Arguments.of("Review notes", true));
    }
}
