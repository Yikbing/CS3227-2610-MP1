package studytracker.command;

import studytracker.model.SessionList;

/** Displays total study time and time grouped by module. */
public class StatsCommand implements Command {
    @Override
    public CommandResult execute(SessionList sessions) {
        return CommandResult.readOnly(sessions.calculateStatistics().toDisplayString());
    }
}
