package OS.exception;

public class SemaphoreException extends RuntimeException {
    public SemaphoreException(String errorMessage) {
        super(errorMessage);
    }
}
