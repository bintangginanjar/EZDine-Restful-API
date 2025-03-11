package restful.api.ezdine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolationException;
import restful.api.ezdine.model.WebResponse;

@RestControllerAdvice
public class CustomExcHandler {
    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> constraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> apiException(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getReason())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> handlerNotFoundException(NoHandlerFoundException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> badCredentialException(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> accessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> authenticationException(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> providerNotFoundException(ProviderNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> insufficientAuthExcepton(InsufficientAuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> usernameNotFoundException(UsernameNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> authenticationServiceException(AuthenticationServiceException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> signatureException(SignatureException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> tokenExpiredException(ExpiredJwtException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }

    @ExceptionHandler
    public ResponseEntity<WebResponse<String>> credentialNotFoundException(AuthenticationCredentialsNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(WebResponse.<String>builder()
                                            .status(false)
                                            .errors(exception.getMessage())
                                            .build());
    }
}
