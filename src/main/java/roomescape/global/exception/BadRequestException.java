package roomescape.global.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RoomescapeException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
