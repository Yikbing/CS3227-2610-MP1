package studytracker.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import studytracker.command.AddCommand;
import studytracker.command.Command;
import studytracker.command.EditCommand;
import studytracker.exception.ParseException;

class ParserTest {
    private final Parser parser = new Parser();

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

    @Test
    void parseEdit_partialFields_preservesOptionalChanges() throws ParseException {
        EditCommand command = assertInstanceOf(EditCommand.class,
                parser.parse("edit 2 d/120 n/Updated notes"));

        assertEquals(2, command.index());
        assertEquals(120, command.descriptor().durationMinutes().orElseThrow());
        assertEquals("Updated notes", command.descriptor().notes().orElseThrow());
        assertEquals(true, command.descriptor().module().isEmpty());
    }

    @Test
    void parseAdd_invalidDate_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse(
                "add m/CS3227 t/Parsing d/90 on/17-08-2026"));
    }

    @Test
    void parseEdit_withoutChanges_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("edit 1"));
    }
}
