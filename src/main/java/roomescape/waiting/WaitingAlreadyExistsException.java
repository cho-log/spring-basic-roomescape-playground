package roomescape.waiting;

public class WaitingAlreadyExistsException extends RuntimeException {
    public WaitingAlreadyExistsException() {
        super("이미 해당 예약에 대기 신청이 되어 있습니다.");
    }
}
