package studytracker;

import java.nio.file.Path;

import studytracker.command.Command;
import studytracker.command.CommandResult;
import studytracker.exception.StudyTrackerException;
import studytracker.model.SessionList;
import studytracker.parser.Parser;
import studytracker.storage.Storage;

/** Coordinates parsing, command execution, the model, and persistent storage. */
public class StudyTracker {
    private static final Path DEFAULT_DATA_PATH = Path.of("data", "sessions.txt");

    private final Parser parser;
    private final Storage storage;
    private final SessionList sessions;
    private final String startupMessage;

    public StudyTracker(Parser parser, Storage storage) {
        this.parser = parser;
        this.storage = storage;
        SessionList loadedSessions;
        String message = "Welcome to Study Tracker. Type 'help' to see the available commands.";
        try {
            loadedSessions = new SessionList(storage.load());
        } catch (StudyTrackerException exception) {
            loadedSessions = new SessionList();
            message = "Your saved data could not be loaded, so an empty session list was opened.\n"
                    + exception.getMessage();
        }
        this.sessions = loadedSessions;
        this.startupMessage = message;
    }

    public static StudyTracker createDefault() {
        return new StudyTracker(new Parser(), new Storage(DEFAULT_DATA_PATH));
    }

    /** Executes one user command and persists successful changes. */
    public CommandResult execute(String input) throws StudyTrackerException {
        Command command = parser.parse(input);
        CommandResult result = command.execute(sessions);
        if (result.dataChanged()) {
            storage.save(sessions.asList());
        }
        return result;
    }

    public String getStartupMessage() {
        return startupMessage;
    }
}
