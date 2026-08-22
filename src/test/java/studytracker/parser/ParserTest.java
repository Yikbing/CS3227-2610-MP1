package studytracker.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import studytracker.command.AddCommand;
import studytracker.command.Command;
import studytracker.command.DeleteCommand;
import studytracker.command.EditCommand;
import studytracker.command.ExitCommand;
import studytracker.command.FilterCommand;
import studytracker.command.FindCommand;
import studytracker.command.HelpCommand;
import studytracker.command.ListCommand;
import studytracker.command.StatsCommand;
import studytracker.exception.ParseException;

class ParserTest {
    private final Parser parser = new Parser();

    // Verifies all fields of a valid add command, including values containing spaces.
    @Test
    void parseAdd_allFields_returnsAddCommand() throws ParseException {
        Command result = parser.parse(
                "add m/CS3227 t/Command parsing d/90 on/2026-08-17 n/Parser tests");

        AddCommand command = assertInstanceOf(AddCommand.class, result);
        assertEquals("CS3227", command.session().module());
        assertEquals("Command parsing", command.session().topic());
        assertEquals(90, command.session().durationMinutes());
        assertEquals(LocalDate.of(2026, 8, 17), command.session().date());
        assertEquals("Parser tests", command.session().notes());
    }

    // Verifies case-insensitive command words, surrounding whitespace, reordered prefixes, and optional notes.
    @Test
    void parseAdd_flexibleDocumentedSyntax_returnsAddCommand() throws ParseException {
        AddCommand command = assertInstanceOf(AddCommand.class, parser.parse(
                "  ADD on/2026-08-17 d/1 t/Parsing basics m/CS3227  "));

        assertEquals("CS3227", command.session().module());
        assertEquals("Parsing basics", command.session().topic());
        assertEquals(1, command.session().durationMinutes());
        assertEquals("", command.session().notes());
    }

    // Verifies that omitted edit fields remain absent while supplied fields are parsed.
    @Test
    void parseEdit_partialFields_preservesOptionalChanges() throws ParseException {
        EditCommand command = assertInstanceOf(EditCommand.class,
                parser.parse("edit 2 d/120 n/Updated notes"));

        assertEquals(2, command.index());
        assertEquals(120, command.descriptor().durationMinutes().orElseThrow());
        assertEquals("Updated notes", command.descriptor().notes().orElseThrow());
        assertTrue(command.descriptor().module().isEmpty());
    }

    // Verifies the documented n/ syntax for clearing existing notes.
    @Test
    void parseEdit_emptyNotes_preservesEmptyValueAsAChange() throws ParseException {
        EditCommand command = assertInstanceOf(EditCommand.class, parser.parse("edit 1 n/"));

        assertEquals("", command.descriptor().notes().orElseThrow());
    }

    // Verifies every editable field can be parsed together into one complete descriptor.
    @Test
    void parseEdit_allSupportedFields_returnsCompleteDescriptor() throws ParseException {
        EditCommand command = assertInstanceOf(EditCommand.class, parser.parse(
                "edit 1 m/CS2100 t/Assembly d/60 on/2026-08-18 n/Tutorial"));

        assertEquals("CS2100", command.descriptor().module().orElseThrow());
        assertEquals("Assembly", command.descriptor().topic().orElseThrow());
        assertEquals(60, command.descriptor().durationMinutes().orElseThrow());
        assertEquals(LocalDate.of(2026, 8, 18), command.descriptor().date().orElseThrow());
        assertEquals("Tutorial", command.descriptor().notes().orElseThrow());
    }

    // Verifies every simple command through one non-repetitive parameterised test.
    @ParameterizedTest
    @MethodSource("simpleCommandCases")
    void parse_simpleCommand_returnsExpectedType(String input, Class<? extends Command> expectedType)
            throws ParseException {
        assertInstanceOf(expectedType, parser.parse(input));
    }

    // Verifies parsing for commands with a single unprefixed argument.
    @Test
    void parseDeleteAndFind_validArguments_returnExpectedCommands() throws ParseException {
        DeleteCommand delete = assertInstanceOf(DeleteCommand.class, parser.parse("delete 2"));
        FindCommand find = assertInstanceOf(FindCommand.class, parser.parse("find command parsing"));

        assertEquals(2, delete.index());
        assertEquals("command parsing", find.keyword());
    }

    // Verifies all combined filter criteria are parsed into the command correctly.
    @Test
    void parseFilter_combinedCriteria_returnsFilterCommand() throws ParseException {
        FilterCommand command = assertInstanceOf(FilterCommand.class, parser.parse(
                "filter to/2026-08-31 m/CS3227 from/2026-08-01"));

        assertEquals("CS3227", command.module());
        assertEquals(LocalDate.of(2026, 8, 1), command.from());
        assertEquals(LocalDate.of(2026, 8, 31), command.to());
    }

    // Verifies each documented filter criterion is valid when supplied on its own.
    @ParameterizedTest
    @MethodSource("singleFilterCases")
    void parseFilter_singleCriterion_returnsFilterCommand(
            String input, String module, LocalDate from, LocalDate to) throws ParseException {
        FilterCommand command = assertInstanceOf(FilterCommand.class, parser.parse(input));

        assertEquals(module, command.module());
        assertEquals(from, command.from());
        assertEquals(to, command.to());
    }

    // Verifies null, empty, and whitespace-only input are all rejected as missing commands.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void parse_missingInput_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies unknown command words cannot be mistaken for supported commands.
    @Test
    void parse_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("show"));
    }

    // Verifies every required add field must be supplied.
    @ParameterizedTest
    @ValueSource(strings = {
        "add t/Parsing d/90 on/2026-08-17",
        "add m/CS3227 d/90 on/2026-08-17",
        "add m/CS3227 t/Parsing on/2026-08-17",
        "add m/CS3227 t/Parsing d/90"
    })
    void parseAdd_missingRequiredField_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies blank required add text is rejected before constructing a session.
    @ParameterizedTest
    @ValueSource(strings = {
        "add m/ t/Parsing d/90 on/2026-08-17",
        "add m/CS3227 t/ d/90 on/2026-08-17"
    })
    void parseAdd_blankRequiredText_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies repeated, unsupported, unprefixed, and uppercase prefixes are rejected.
    @ParameterizedTest
    @ValueSource(strings = {
        "add m/CS3227 m/CS2100 t/Parsing d/90 on/2026-08-17",
        "add m/CS3227 t/Parsing d/90 on/2026-08-17 from/2026-08-01",
        "add unexpected m/CS3227 t/Parsing d/90 on/2026-08-17",
        "add M/CS3227 t/Parsing d/90 on/2026-08-17"
    })
    void parseAdd_invalidFieldStructure_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies invalid numeric partitions while retaining one valid boundary in a separate pass test.
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "minutes", "999999999999999999999"})
    void parseAdd_invalidDuration_throwsParseException(String duration) {
        assertThrows(ParseException.class, () -> parser.parse(
                "add m/CS3227 t/Parsing d/" + duration + " on/2026-08-17"));
    }

    // Verifies invalid formats and impossible calendar dates are rejected.
    @ParameterizedTest
    @ValueSource(strings = {"17-08-2026", "2026-02-30"})
    void parseAdd_invalidDate_throwsParseException(String date) {
        assertThrows(ParseException.class, () -> parser.parse(
                "add m/CS3227 t/Parsing d/90 on/" + date));
    }

    // Verifies invalid index partitions for commands that require positive indexes.
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "first", "999999999999999999999"})
    void parseDelete_invalidIndex_throwsParseException(String index) {
        assertThrows(ParseException.class, () -> parser.parse("delete " + index));
    }

    // Verifies delete rejects a missing index and unexpected text after an index.
    @ParameterizedTest
    @ValueSource(strings = {"delete", "delete 1 extra"})
    void parseDelete_invalidArgumentStructure_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies find requires a non-blank keyword.
    @ParameterizedTest
    @ValueSource(strings = {"find", "find   "})
    void parseFind_blankKeyword_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies that edit requires at least one field after its index.
    @Test
    void parseEdit_withoutChanges_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("edit 1"));
    }

    // Verifies invalid edit indexes and field values are rejected consistently.
    @ParameterizedTest
    @ValueSource(strings = {
        "edit 0 t/Storage",
        "edit first t/Storage",
        "edit 1 m/",
        "edit 1 t/",
        "edit 1 d/0",
        "edit 1 d/minutes",
        "edit 1 on/2026-02-30"
    })
    void parseEdit_invalidIndexOrFieldValue_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies edit rejects repeated and command-inappropriate prefixes.
    @ParameterizedTest
    @ValueSource(strings = {"edit 1 t/One t/Two", "edit 1 from/2026-08-01"})
    void parseEdit_invalidFieldStructure_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies filter criteria are required and chronological ranges are valid.
    @ParameterizedTest
    @ValueSource(strings = {"filter", "filter from/2026-08-31 to/2026-08-01"})
    void parseFilter_invalidCriteria_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies malformed, blank, repeated, and unsupported filter values are rejected.
    @ParameterizedTest
    @ValueSource(strings = {
        "filter m/",
        "filter from/17-08-2026",
        "filter to/2026-02-30",
        "filter m/CS3227 m/CS2100",
        "filter t/Parsing"
    })
    void parseFilter_invalidField_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    // Verifies commands documented as argument-free consistently reject extra text.
    @ParameterizedTest
    @ValueSource(strings = {"list extra", "stats extra", "help extra", "exit extra"})
    void parse_noArgumentCommandWithExtraText_throwsParseException(String input) {
        assertThrows(ParseException.class, () -> parser.parse(input));
    }

    private static Stream<Arguments> simpleCommandCases() {
        return Stream.of(
                Arguments.of("list", ListCommand.class),
                Arguments.of("stats", StatsCommand.class),
                Arguments.of("help", HelpCommand.class),
                Arguments.of("exit", ExitCommand.class));
    }

    private static Stream<Arguments> singleFilterCases() {
        return Stream.of(
                Arguments.of("filter m/CS3227", "CS3227", null, null),
                Arguments.of("filter from/2026-08-01", null, LocalDate.of(2026, 8, 1), null),
                Arguments.of("filter to/2026-08-31", null, null, LocalDate.of(2026, 8, 31)));
    }
}
