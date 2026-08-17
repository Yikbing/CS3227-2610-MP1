package studytracker.command;

import studytracker.exception.StudyTrackerException;
import studytracker.model.SessionList;

/** Represents one parsed user operation. */
public interface Command {
    CommandResult execute(SessionList sessions) throws StudyTrackerException;
}
