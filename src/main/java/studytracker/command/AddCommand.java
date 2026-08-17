package studytracker.command;

import studytracker.model.SessionList;
import studytracker.model.StudySession;

/** Adds one completed study session. */
public record AddCommand(StudySession session) implements Command {
    @Override
    public CommandResult execute(SessionList sessions) {
        sessions.add(session);
        return CommandResult.changed("Recorded study session:\n" + session.toDisplayString());
    }
}
