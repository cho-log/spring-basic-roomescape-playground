package roomescape.time;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String timeValue;

    public Time(Long id, String timeValue) {
        this.id = id;
        this.timeValue = timeValue;
    }

    public Time(String timeValue) {
        this.timeValue = timeValue;
    }

    public Time() {

    }

    public Long getId() {
        return id;
    }

    public String getTime() {
        return timeValue;
    }

    public String getValue() {
        return timeValue;
    }
}
