package studytracker.exception;

/** Indicates that saved data could not be read or written safely. */
public class StorageException extends StudyTrackerException {
    private static final long serialVersionUID = 1L;

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(String message) {
        super(message);
    }
}
