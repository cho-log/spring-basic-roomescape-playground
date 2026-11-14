package roomescape.time;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_value")
    private LocalTime value;

    private boolean deleted = false;

    public void delete() {
        this.deleted = true;
    }

    public Time(Long id, LocalTime value) {
        this.id = id;
        this.value = value;
    }

    public Time(LocalTime value) {
        this.value = value;
    }

    public Time() {

    }

    public Long getId() {
        return id;
    }

    public LocalTime getValue() {
        return value;
    }
}
