package roomescape.waiting;

public class WaitingAccessDeniedException extends RuntimeException {
    public WaitingAccessDeniedException() {
        super("본인의 예약 대기만 취소할 수 있습니다.");
    }
}
