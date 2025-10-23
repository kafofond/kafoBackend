package kafofond.exception;

/**
 * Exception levée lorsqu'un commentaire est obligatoire mais non fourni
 */
public class CommentaireObligatoireException extends RuntimeException {
    
    public CommentaireObligatoireException(String message) {
        super(message);
    }
    
    public CommentaireObligatoireException(String message, Throwable cause) {
        super(message, cause);
    }
}
