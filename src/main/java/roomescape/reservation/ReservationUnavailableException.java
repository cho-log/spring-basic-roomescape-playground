package roomescape.reservation;

public class ReservationUnavailableException extends RuntimeException {
    public ReservationUnavailableException() {
        super("예약 대기가 가능한 예약이 존재하지 않습니다.");
    }
}
