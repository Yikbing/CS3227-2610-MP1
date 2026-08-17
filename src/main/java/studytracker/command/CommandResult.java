package studytracker.command;

/** Tells the UI what to show and the application whether to save or exit. */
public record CommandResult(String feedback, boolean dataChanged, boolean exit) {
    public static CommandResult readOnly(String feedback) {
        return new CommandResult(feedback, false, false);
    }

    public static CommandResult changed(String feedback) {
        return new CommandResult(feedback, true, false);
    }
}
