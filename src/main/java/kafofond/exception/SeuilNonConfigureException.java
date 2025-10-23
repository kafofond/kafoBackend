package kafofond.exception;

/**
 * Exception levée lorsqu'un seuil de validation n'est pas configuré
 */
public class SeuilNonConfigureException extends RuntimeException {
    
    public SeuilNonConfigureException(String message) {
        super(message);
    }
    
    public SeuilNonConfigureException(String message, Throwable cause) {
        super(message, cause);
    }
}
