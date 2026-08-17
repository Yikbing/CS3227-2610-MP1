package studytracker.model;

import java.time.LocalDate;
import java.util.Optional;

/** Describes only the fields supplied in an edit command. */
public record EditDescriptor(Optional<String> module, Optional<String> topic,
                             Optional<Integer> durationMinutes, Optional<LocalDate> date,
                             Optional<String> notes) {
    public EditDescriptor {
        module = nonNull(module);
        topic = nonNull(topic);
        durationMinutes = nonNull(durationMinutes);
        date = nonNull(date);
        notes = nonNull(notes);
    }

    private static <T> Optional<T> nonNull(Optional<T> value) {
        return value == null ? Optional.empty() : value;
    }

    public boolean isEmpty() {
        return module.isEmpty() && topic.isEmpty() && durationMinutes.isEmpty()
                && date.isEmpty() && notes.isEmpty();
    }

    /** Applies these changes while preserving every unspecified field. */
    public StudySession applyTo(StudySession original) {
        return new StudySession(module.orElse(original.module()), topic.orElse(original.topic()),
                durationMinutes.orElse(original.durationMinutes()), date.orElse(original.date()),
                notes.orElse(original.notes()));
    }
}
