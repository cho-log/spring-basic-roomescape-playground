package roomescape.reservation;

public class ReservationAlreadyExistsException extends RuntimeException {
    public ReservationAlreadyExistsException() {
        super("이미 해당 예약을 신청한 사용자입니다.");
    }
}
