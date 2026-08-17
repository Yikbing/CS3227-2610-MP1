package studytracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** An immutable record of one completed period of study. */
public record StudySession(String module, String topic, int durationMinutes,
                           LocalDate date, String notes) {
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public StudySession {
        module = requireText(module, "Module");
        topic = requireText(topic, "Topic");
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be a positive number of minutes.");
        }
        Objects.requireNonNull(date, "Date must not be null.");
        notes = notes == null ? "" : notes.strip();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.strip();
    }

    /** Returns a compact representation suitable for lists and command feedback. */
    public String toDisplayString() {
        String base = "%s — %s | %s | %s".formatted(
                module, topic, formatDuration(durationMinutes), date.format(DISPLAY_DATE));
        return notes.isEmpty() ? base : base + " | " + notes;
    }

    public static String formatDuration(int minutes) {
        int hours = minutes / 60;
        int remainder = minutes % 60;
        if (hours == 0) {
            return remainder + " min";
        }
        return remainder == 0 ? hours + " h" : hours + " h " + remainder + " min";
    }
}
