package pl.javakurs.dname074.model.exception;

import pl.javakurs.dname074.dto.exception.MedicalClinicExceptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<MedicalClinicExceptionDto> handleAuth(AuthenticationCredentialsNotFoundException ex) {
        String message = ex.getMessage();
        logError(ex, message);
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(
                new MedicalClinicExceptionDto(message, status));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MedicalClinicExceptionDto> handleForbidden(AccessDeniedException ex) {
        String message = ex.getMessage();
        logError(ex, message);
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(
                new MedicalClinicExceptionDto(message, status));
    }

    private void logError(Exception ex, String message) {
        String logMessage = String.format("Exception occured: %s, message: %s",ex.getClass().getName(), ex.getMessage());
        log.error(logMessage);
    }
}