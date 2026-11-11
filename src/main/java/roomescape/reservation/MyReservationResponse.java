package roomescape.reservation;

public class MyReservationResponse {
    private long id;
    private Long reservationId;
    private String theme;
    private String date;
    private String time;
    private String status;

    public MyReservationResponse() {
    }

    public MyReservationResponse(Long reservationId, String theme, String date, String time, String status) {
        this(reservationId, reservationId, theme, date, time, status);
    }

    public MyReservationResponse(long id, Long reservationId, String theme, String date, String time, String status) {
        this.id = id;
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
