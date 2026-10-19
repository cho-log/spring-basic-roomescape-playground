package roomescape.waiting;

public class WaitingRequest {

    private String date;
    private Long time;
    private Long theme;

    public WaitingRequest() {
    }

    public WaitingRequest(String date, Long time, Long theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public String getDate() {
        return date;
    }

    public Long getTheme() {
        return theme;
    }

    public Long getTime() {
        return time;
    }
}
