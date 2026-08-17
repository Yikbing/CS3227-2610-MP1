package studytracker.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** An immutable summary derived from recorded study sessions. */
public record StudyStatistics(int sessionCount, int totalMinutes,
                              Map<String, Integer> minutesByModule) {
    public StudyStatistics {
        minutesByModule = Map.copyOf(new LinkedHashMap<>(minutesByModule));
    }

    public String toDisplayString() {
        if (sessionCount == 0) {
            return "No study sessions have been recorded yet.";
        }
        StringBuilder result = new StringBuilder("Study summary\n")
                .append("Sessions: ").append(sessionCount).append('\n')
                .append("Total time: ").append(StudySession.formatDuration(totalMinutes));
        minutesByModule.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> result.append("\n- ").append(entry.getKey()).append(": ")
                        .append(StudySession.formatDuration(entry.getValue())));
        return result.toString();
    }
}
