package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationResponse {
    private Long id;
    private String name;
    private String theme;
    private LocalDate date;
    private LocalTime time;

    public ReservationResponse(Long id, String name, String theme, LocalDate date, LocalTime time) {
        this.id = id;
        this.name = name;
        this.theme = theme;
        this.date = date;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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
}
