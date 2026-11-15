package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RoomescapeException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
