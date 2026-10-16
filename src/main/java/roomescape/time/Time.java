package roomescape.time;

import jakarta.persistence.*;

@Entity
public class Time {
    private boolean deleted = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_value")
    private String value;

    public Time(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public Time(String value) {
        this.value = value;
    }

    public Time() {

    }

    public void delete() {
        this.deleted = true;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
