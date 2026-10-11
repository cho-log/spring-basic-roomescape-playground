package roomescape.exception.auth;

public class ForbiddenAdminOperationException extends RuntimeException {
    public ForbiddenAdminOperationException(String message) {
        super(message);
    }
}
