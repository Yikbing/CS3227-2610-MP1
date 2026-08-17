package studytracker.command;

import studytracker.model.SessionList;

/** Displays the supported command formats. */
public class HelpCommand implements Command {
    private static final String HELP = """
            Available commands:
            add m/MODULE t/TOPIC d/MINUTES on/YYYY-MM-DD [n/NOTES]
            list
            edit INDEX [m/MODULE] [t/TOPIC] [d/MINUTES] [on/YYYY-MM-DD] [n/NOTES]
            delete INDEX
            find KEYWORD
            filter [m/MODULE] [from/YYYY-MM-DD] [to/YYYY-MM-DD]
            stats
            help
            exit

            Example: add m/CS3227 t/Command parsing d/90 on/2026-08-17 n/Parser tests
            """;

    @Override
    public CommandResult execute(SessionList sessions) {
        return CommandResult.readOnly(HELP.strip());
    }
}
