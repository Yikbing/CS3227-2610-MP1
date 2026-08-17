package studytracker.exception;

/** Indicates that a command or one of its arguments is invalid. */
public class ParseException extends StudyTrackerException {
    private static final long serialVersionUID = 1L;

    public ParseException(String message) {
        super(message);
    }
}
