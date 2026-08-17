package studytracker.exception;

/** Base checked exception for errors that can be explained to the user. */
public class StudyTrackerException extends Exception {
    private static final long serialVersionUID = 1L;

    public StudyTrackerException(String message) {
        super(message);
    }

    public StudyTrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}
