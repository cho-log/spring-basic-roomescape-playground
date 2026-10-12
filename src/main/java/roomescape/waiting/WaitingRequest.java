package roomescape.waiting;

import java.time.LocalDate;

public class WaitingRequest {
    private LocalDate date;
    private Long theme;
    private Long time;

    public LocalDate getDate() {
        return date;
    }

    public Long getTheme() {
        return theme;
    }

    public Long getTime() {
        return time;
    }
}