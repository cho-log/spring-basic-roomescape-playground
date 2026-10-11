package roomescape.exception.reservation;

public class ForbiddenReservationException extends RuntimeException {
    public ForbiddenReservationException(String message) {
        super(message);
    }
}
