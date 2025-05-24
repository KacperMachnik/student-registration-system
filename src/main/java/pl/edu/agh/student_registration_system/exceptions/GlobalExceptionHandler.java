package pl.edu.agh.student_registration_system.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.edu.agh.student_registration_system.payload.response.MessageResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> response = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            response.put(fieldName, message);
        });

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EnrollmentConflictException.class)
    public ResponseEntity<MessageResponse> handleEnrollmentConflictException(EnrollmentConflictException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Data integrity violation. This could be due to a duplicate entry or a violation of a database constraint.";
        if (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null) {
            if (ex.getMostSpecificCause().getMessage().toLowerCase().contains("unique constraint") ||
                    ex.getMostSpecificCause().getMessage().toLowerCase().contains("duplicate key")) {
                message = "An entry with this information already exists. Please check for duplicates.";
            }
        } else if (ex.getMessage() != null) {
            if (ex.getMessage().toLowerCase().contains("unique constraint") ||
                    ex.getMessage().toLowerCase().contains("duplicate key")) {
                message = "An entry with this information already exists. Please check for duplicates.";
            }
        }
        MessageResponse response = new MessageResponse(message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<MessageResponse> handleAuthenticationFailedException(AuthenticationFailedException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<MessageResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        MessageResponse response = new MessageResponse("Access Denied: You do not have permission to perform this action.");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DeletionBlockedException.class)
    public ResponseEntity<MessageResponse> handleDeletionBlockedException(DeletionBlockedException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MessageResponse> handleIllegalStateException(IllegalStateException ex) {
        MessageResponse response = new MessageResponse("An internal error occurred: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IndexNumberGenerationException.class)
    public ResponseEntity<MessageResponse> handleIndexNumberGenerationException(IndexNumberGenerationException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<MessageResponse> handleInvalidOperationException(InvalidOperationException ex) {
        MessageResponse response = new MessageResponse(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(UnexpectedRollbackException.class)
    public ResponseEntity<MessageResponse> handleUnexpectedRollbackException(UnexpectedRollbackException ex) {
        Throwable cause = ex.getRootCause();
        String message = "Transaction unexpectedly rolled back.";
        if (cause != null) {
            message += " Root cause: " + cause.getMessage();
        }
        MessageResponse response = new MessageResponse(message);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleGenericRuntimeException(RuntimeException ex) {
        MessageResponse response = new MessageResponse("An unexpected error occurred. Please try again later. Error:"+ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGenericException(Exception ex) {
        MessageResponse response = new MessageResponse("An unexpected server error occurred: "+ ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}