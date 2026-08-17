package studytracker.command;

import studytracker.model.SessionList;

/** Requests a graceful application exit. */
public class ExitCommand implements Command {
    @Override
    public CommandResult execute(SessionList sessions) {
        return new CommandResult("Your study sessions have been saved. Goodbye!", false, true);
    }
}
