package studytracker.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import studytracker.exception.ParseException;

/** Reusable conversions for command arguments. */
final class ParserUtil {
    private ParserUtil() {
    }

    static int parsePositiveInt(String value, String label) throws ParseException {
        try {
            int result = Integer.parseInt(value);
            if (result <= 0) {
                throw new NumberFormatException();
            }
            return result;
        } catch (NumberFormatException exception) {
            throw new ParseException(label + " must be a positive whole number.");
        }
    }

    static LocalDate parseDate(String value, String label) throws ParseException {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ParseException(label + " must use YYYY-MM-DD, for example 2026-08-17.");
        }
    }

    static String requireNonBlank(String value, String label) throws ParseException {
        if (value == null || value.isBlank()) {
            throw new ParseException(label + " must not be blank.");
        }
        return value.strip();
    }
}
