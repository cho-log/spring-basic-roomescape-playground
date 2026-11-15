package roomescape;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import roomescape.exception.auth.ForbiddenAdminOperationException;
import roomescape.exception.auth.InvalidAuthenticationException;
import roomescape.exception.member.DuplicateMemberException;
import roomescape.exception.member.InvalidMemberException;
import roomescape.exception.member.NotFoundMemberException;
import roomescape.exception.reservation.DuplicateReservationException;
import roomescape.exception.reservation.ForbiddenReservationException;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.theme.InvalidThemeException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.exception.time.DuplicateTimeException;
import roomescape.exception.time.InvalidTimeException;
import roomescape.exception.time.NotFoundTimeException;
import roomescape.exception.waiting.DuplicateWaitingException;
import roomescape.exception.waiting.ForbiddenWaitingException;
import roomescape.exception.waiting.NotFoundWaitingException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidAuthenticationException.class)
    public ResponseEntity<Void> handleInvalidAuthenticationException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(InvalidTimeException.class)
    public ResponseEntity<Void> handleInvalidTimeException() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(InvalidMemberException.class)
    public ResponseEntity<Void> handleInvalidMemberException() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<Void> handleDuplicateMemberException() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(InvalidReservationException.class)
    public ResponseEntity<Void> handleInvalidReservationException() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(NotFoundMemberException.class)
    public ResponseEntity<Void> handleNotFoundMemberException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(NotFoundTimeException.class)
    public ResponseEntity<Void> handleNotFoundTimeException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(NotFoundThemeException.class)
    public ResponseEntity<Void> handleNotFoundThemeException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(NotFoundReservationException.class)
    public ResponseEntity<Void> handleNotFoundReservationException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InvalidThemeException.class)
    public ResponseEntity<Void> handleInvalidThemeException() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(DuplicateTimeException.class)
    public ResponseEntity<Void> handleDuplicateTimeException() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(DuplicateReservationException.class)
    public ResponseEntity<Void> handleDuplicateReservationException() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(NotFoundWaitingException.class)
    public ResponseEntity<Void> handleNotFoundWaitingException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DuplicateWaitingException.class)
    public ResponseEntity<Void> handleDuplicateWaitingException() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(ForbiddenWaitingException.class)
    public ResponseEntity<Void> handleForbiddenWaitingException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(ForbiddenReservationException.class)
    public ResponseEntity<Void> handleForbiddenReservationException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(ForbiddenAdminOperationException.class)
    public ResponseEntity<Void> handleForbiddenAdminOperationException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
