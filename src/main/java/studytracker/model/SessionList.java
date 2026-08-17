package studytracker.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import studytracker.exception.StudyTrackerException;

/** Owns the collection of study sessions and its domain operations. */
public class SessionList {
    private final List<StudySession> sessions;

    public SessionList() {
        this(List.of());
    }

    public SessionList(List<StudySession> sessions) {
        this.sessions = new ArrayList<>(sessions);
    }

    public void add(StudySession session) {
        sessions.add(session);
    }

    public StudySession delete(int oneBasedIndex) throws StudyTrackerException {
        return sessions.remove(toInternalIndex(oneBasedIndex));
    }

    public StudySession edit(int oneBasedIndex, EditDescriptor descriptor) throws StudyTrackerException {
        int index = toInternalIndex(oneBasedIndex);
        StudySession edited = descriptor.applyTo(sessions.get(index));
        sessions.set(index, edited);
        return edited;
    }

    public List<StudySession> asList() {
        return List.copyOf(sessions);
    }

    public List<StudySession> find(String keyword) {
        String normalised = keyword.toLowerCase(Locale.ROOT);
        return sessions.stream()
                .filter(session -> session.module().toLowerCase(Locale.ROOT).contains(normalised)
                        || session.topic().toLowerCase(Locale.ROOT).contains(normalised)
                        || session.notes().toLowerCase(Locale.ROOT).contains(normalised))
                .toList();
    }

    public List<StudySession> filter(String module, LocalDate from, LocalDate to) {
        return sessions.stream()
                .filter(session -> module == null || session.module().equalsIgnoreCase(module))
                .filter(session -> from == null || !session.date().isBefore(from))
                .filter(session -> to == null || !session.date().isAfter(to))
                .sorted(Comparator.comparing(StudySession::date).reversed())
                .toList();
    }

    public StudyStatistics calculateStatistics() {
        Map<String, Integer> minutesByModule = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sessions.forEach(session -> minutesByModule.merge(
                session.module(), session.durationMinutes(), Integer::sum));
        int totalMinutes = sessions.stream().mapToInt(StudySession::durationMinutes).sum();
        return new StudyStatistics(sessions.size(), totalMinutes, minutesByModule);
    }

    private int toInternalIndex(int oneBasedIndex) throws StudyTrackerException {
        if (oneBasedIndex < 1 || oneBasedIndex > sessions.size()) {
            throw new StudyTrackerException("Session number must be between 1 and " + sessions.size() + ".");
        }
        return oneBasedIndex - 1;
    }
}
