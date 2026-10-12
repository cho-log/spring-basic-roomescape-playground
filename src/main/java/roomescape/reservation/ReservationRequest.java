package roomescape.reservation;

import java.time.LocalDate;

public class ReservationRequest {
    private String name;
    private LocalDate date;
    private Long theme;
    private Long time;

    public String getName() {
        return name;
    }

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
