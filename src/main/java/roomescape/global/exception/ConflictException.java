package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends RoomescapeException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
