package studytracker.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class StudyStatisticsTest {
    // Verifies the dedicated user-facing message for an empty study history.
    @Test
    void toDisplayString_emptyStatistics_returnsEmptyMessage() {
        StudyStatistics statistics = new StudyStatistics(0, 0, Map.of());

        assertEquals("No study sessions have been recorded yet.", statistics.toDisplayString());
    }

    // Verifies totals, duration formatting, and case-insensitive module display ordering.
    @Test
    void toDisplayString_populatedStatistics_formatsAndSortsModules() {
        Map<String, Integer> minutes = new LinkedHashMap<>();
        minutes.put("CS3227", 90);
        minutes.put("cs2100", 60);
        StudyStatistics statistics = new StudyStatistics(2, 150, minutes);

        String display = statistics.toDisplayString();

        assertTrue(display.contains("Sessions: 2"));
        assertTrue(display.contains("Total time: 2 h 30 min"));
        assertTrue(display.indexOf("cs2100: 1 h") < display.indexOf("CS3227: 1 h 30 min"));
    }

    // Verifies that later mutation of the constructor's map cannot alter stored statistics.
    @Test
    void constructor_sourceMapIsLaterModified_preservesSnapshot() {
        Map<String, Integer> source = new LinkedHashMap<>();
        source.put("CS3227", 90);
        StudyStatistics statistics = new StudyStatistics(1, 90, source);

        source.put("CS2100", 60);

        assertEquals(Map.of("CS3227", 90), statistics.minutesByModule());
    }
}
