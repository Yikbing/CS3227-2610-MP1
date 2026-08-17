package studytracker.storage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;

import studytracker.exception.StorageException;
import studytracker.model.StudySession;

/** Encodes sessions in a delimiter-safe, versioned text format. */
final class SessionFileCodec {
    private static final String VERSION = "1";
    private static final String DELIMITER = "\\t";

    String encode(StudySession session) {
        return String.join("\t", VERSION, encodeText(session.module()), encodeText(session.topic()),
                Integer.toString(session.durationMinutes()), session.date().toString(), encodeText(session.notes()));
    }

    StudySession decode(String line, int lineNumber) throws StorageException {
        String[] fields = line.split(DELIMITER, -1);
        if (fields.length != 6 || !VERSION.equals(fields[0])) {
            throw invalidLine(lineNumber);
        }
        try {
            return new StudySession(decodeText(fields[1]), decodeText(fields[2]),
                    Integer.parseInt(fields[3]), LocalDate.parse(fields[4]), decodeText(fields[5]));
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            throw new StorageException("Saved data is invalid at line " + lineNumber + ".", exception);
        }
    }

    private String encodeText(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decodeText(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private StorageException invalidLine(int lineNumber) {
        return new StorageException("Saved data has an unsupported format at line " + lineNumber + ".");
    }
}
