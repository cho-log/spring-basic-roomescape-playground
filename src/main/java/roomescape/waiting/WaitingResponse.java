package roomescape.waiting;

public class WaitingResponse {
    private Long id;
    private Long waitingNumber;

    public WaitingResponse() {
    }

    public WaitingResponse(Long id, Long waitingNumber) {
        this.id = id;
        this.waitingNumber = waitingNumber;
    }

    public long getId() {
        return id;
    }

    public Long getWaitingNumber() {
        return waitingNumber;
    }
}
