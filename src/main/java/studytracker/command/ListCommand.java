package studytracker.command;

import studytracker.model.SessionList;

/** Lists all sessions in their stored order. */
public class ListCommand implements Command {
    @Override
    public CommandResult execute(SessionList sessions) {
        return CommandResult.readOnly(CommandMessages.formatSessions("Study history:", sessions.asList()));
    }
}
