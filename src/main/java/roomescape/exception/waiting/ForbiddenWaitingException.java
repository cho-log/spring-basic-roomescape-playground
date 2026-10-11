package roomescape.exception.waiting;

public class ForbiddenWaitingException extends RuntimeException {
    public ForbiddenWaitingException(String message) {
        super(message);
    }
}
