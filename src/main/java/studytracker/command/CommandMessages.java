package studytracker.command;

import java.util.List;

import studytracker.model.StudySession;

/** Shared formatting for command results. */
final class CommandMessages {
    private CommandMessages() {
    }

    static String formatSessions(String heading, List<StudySession> sessions) {
        if (sessions.isEmpty()) {
            return heading + "\nNo matching study sessions.";
        }
        StringBuilder result = new StringBuilder(heading);
        for (int index = 0; index < sessions.size(); index++) {
            result.append("\n").append(index + 1).append(". ")
                    .append(sessions.get(index).toDisplayString());
        }
        return result.toString();
    }
}
