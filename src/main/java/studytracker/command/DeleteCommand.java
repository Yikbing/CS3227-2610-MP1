package studytracker.command;

import studytracker.exception.StudyTrackerException;
import studytracker.model.SessionList;
import studytracker.model.StudySession;

/** Deletes a session identified by its displayed list number. */
public record DeleteCommand(int index) implements Command {
    @Override
    public CommandResult execute(SessionList sessions) throws StudyTrackerException {
        StudySession deleted = sessions.delete(index);
        return CommandResult.changed("Deleted study session:\n" + deleted.toDisplayString());
    }
}
