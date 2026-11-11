package roomescape;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.auth.InvalidTokenException;

@ControllerAdvice
public class ExceptionController {
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Void> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity.status(401).build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Void> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleRuntimeException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.badRequest().build();
    }
}
