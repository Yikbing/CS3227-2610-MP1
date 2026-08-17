package studytracker.command;

import java.time.LocalDate;

import studytracker.model.SessionList;

/** Filters sessions by an optional module and inclusive date range. */
public record FilterCommand(String module, LocalDate from, LocalDate to) implements Command {
    @Override
    public CommandResult execute(SessionList sessions) {
        return CommandResult.readOnly(CommandMessages.formatSessions(
                "Filtered study sessions:", sessions.filter(module, from, to)));
    }
}
