package roomescape.time;

import java.time.LocalTime;

public class AvailableTime {
    private Long timeId;
    private LocalTime time;
    private boolean booked;

    public AvailableTime(Long timeId, LocalTime time, boolean booked) {
        this.timeId = timeId;
        this.time = time;
        this.booked = booked;
    }

    public Long getTimeId() {
        return timeId;
    }

    public LocalTime getTime() {
        return time;
    }

    public boolean isBooked() {
        return booked;
    }
}
