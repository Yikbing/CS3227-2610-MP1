package studytracker.command;

import studytracker.model.SessionList;

/** Finds sessions whose module, topic, or notes contain a keyword. */
public record FindCommand(String keyword) implements Command {
    @Override
    public CommandResult execute(SessionList sessions) {
        return CommandResult.readOnly(CommandMessages.formatSessions(
                "Sessions matching '" + keyword + "':", sessions.find(keyword)));
    }
}
