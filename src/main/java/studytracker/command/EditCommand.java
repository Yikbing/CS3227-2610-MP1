package studytracker.command;

import studytracker.exception.StudyTrackerException;
import studytracker.model.EditDescriptor;
import studytracker.model.SessionList;
import studytracker.model.StudySession;

/** Replaces the supplied fields of an existing study session. */
public record EditCommand(int index, EditDescriptor descriptor) implements Command {
    @Override
    public CommandResult execute(SessionList sessions) throws StudyTrackerException {
        StudySession edited = sessions.edit(index, descriptor);
        return CommandResult.changed("Updated study session:\n" + edited.toDisplayString());
    }
}
