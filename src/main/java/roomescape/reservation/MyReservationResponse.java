package roomescape.reservation;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MyReservationResponse {

    Long reservationId;
    String theme;
    String date;
    String time;
    String status;

    public MyReservationResponse(Long reservationId, String theme, String date, String time,
        String status) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public MyReservationResponse() {
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    @JsonIgnore
    public Long getId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public String getTime() {
        return time;
    }

}
