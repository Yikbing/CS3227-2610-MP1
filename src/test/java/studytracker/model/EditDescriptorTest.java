package studytracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EditDescriptorTest {
    private static final LocalDate ORIGINAL_DATE = LocalDate.of(2026, 8, 17);

    // Verifies defensive normalisation when callers supply null Optional references.
    @Test
    void constructor_nullOptionals_normalisesToEmpty() {
        EditDescriptor descriptor = new EditDescriptor(null, null, null, null, null);

        assertTrue(descriptor.isEmpty());
    }

    // Verifies that a descriptor with no supplied fields reports itself as empty.
    @Test
    void isEmpty_withNoChanges_returnsTrue() {
        EditDescriptor descriptor = new EditDescriptor(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        assertTrue(descriptor.isEmpty());
    }

    // Verifies that supplying any one editable field makes the descriptor non-empty.
    @ParameterizedTest
    @MethodSource("singleChangeDescriptors")
    void isEmpty_withAnyChange_returnsFalse(EditDescriptor descriptor) {
        assertFalse(descriptor.isEmpty());
    }

    // Verifies that applying a complete descriptor replaces every original field.
    @Test
    void applyTo_allFieldsSupplied_replacesEveryField() {
        StudySession original = new StudySession("CS3227", "Parsing", 30, ORIGINAL_DATE, "Old");
        LocalDate newDate = LocalDate.of(2026, 8, 18);
        EditDescriptor descriptor = new EditDescriptor(Optional.of("CS2100"), Optional.of("Assembly"),
                Optional.of(60), Optional.of(newDate), Optional.of("New"));

        StudySession edited = descriptor.applyTo(original);

        assertEquals(new StudySession("CS2100", "Assembly", 60, newDate, "New"), edited);
    }

    private static Stream<Arguments> singleChangeDescriptors() {
        return Stream.of(
                Arguments.of(new EditDescriptor(Optional.of("CS2100"), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty())),
                Arguments.of(new EditDescriptor(Optional.empty(), Optional.of("Assembly"),
                        Optional.empty(), Optional.empty(), Optional.empty())),
                Arguments.of(new EditDescriptor(Optional.empty(), Optional.empty(),
                        Optional.of(60), Optional.empty(), Optional.empty())),
                Arguments.of(new EditDescriptor(Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.of(LocalDate.of(2026, 8, 18)), Optional.empty())),
                Arguments.of(new EditDescriptor(Optional.empty(), Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.of("Notes"))));
    }
}
