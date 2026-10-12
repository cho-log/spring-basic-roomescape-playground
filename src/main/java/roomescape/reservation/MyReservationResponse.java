package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public class MyReservationResponse {
    private Long id;
    private String theme;
    private LocalDate date;
    private LocalTime time;
    private String status;

    public MyReservationResponse(Long id, String theme, LocalDate date, LocalTime time, String status) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
