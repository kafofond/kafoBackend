package kafofond.exception;

import kafofond.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions
 * Intercepte toutes les exceptions et retourne des réponses d'erreur standardisées
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Gestion des erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Erreur de validation : {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Erreur de validation des données")
                .code("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .details(errors.toString())
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Gestion des erreurs d'authentification
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex, WebRequest request) {
        log.error("Erreur d'authentification : {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Erreur d'authentification")
                .code("AUTHENTICATION_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .details(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Gestion des erreurs d'autorisation
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Erreur d'autorisation : {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Accès refusé")
                .code("ACCESS_DENIED")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .details("Vous n'avez pas les droits nécessaires pour effectuer cette action")
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Ressource non trouvée (générique pour DocumentNotFoundException et RessourceNonTrouveeException)
     */
    @ExceptionHandler({DocumentNotFoundException.class, RessourceNonTrouveeException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(RuntimeException ex, WebRequest request) {
        log.error("Ressource non trouvée : {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .code("NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Gestion des erreurs spécifiques du domaine
     */
    @ExceptionHandler(OperationNonAutoriseeException.class)
    public ResponseEntity<ErrorResponse> handleOperationNonAutorisee(OperationNonAutoriseeException ex, WebRequest request) {
        log.error("Opération non autorisée : {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .code("OPERATION_NOT_ALLOWED")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

//    @ExceptionHandler(CommentaireObligatoireException.class)
//    public ResponseEntity<ErrorResponse> handleCommentaireObligatoireException(CommentaireObligatoireException ex, WebRequest request) {
//        log.error("Commentaire obligatoire manquant : {}", ex.getMessage());
//
//        ErrorResponse errorResponse = ErrorResponse.builder()
//                .message(ex.getMessage())
//                .code("COMMENTAIRE_OBLIGATOIRE")
//                .timestamp(LocalDateTime.now())
//                .path(request.getDescription(false).replace("uri=", ""))
//                .build();
//
//        return ResponseEntity.badRequest().body(errorResponse);
//    }

    @ExceptionHandler(SeuilNonConfigureException.class)
    public ResponseEntity<ErrorResponse> handleSeuilNonConfigureException(SeuilNonConfigureException ex, WebRequest request) {
        log.error("Seuil non configuré : {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .code("SEUIL_NON_CONFIGURE")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Gestion des erreurs d'argument invalide
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.error("Argument invalide : {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .code("INVALID_ARGUMENT")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Gestion des erreurs de runtime
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Erreur runtime : {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .code("RUNTIME_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Gestion des erreurs génériques
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Erreur générique : {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message("Une erreur interne s'est produite")
                .code("INTERNAL_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .details("Veuillez contacter l'administrateur")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
