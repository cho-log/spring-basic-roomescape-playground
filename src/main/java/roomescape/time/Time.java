package roomescape.time;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "time")
public class Time {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "time_value", nullable = false, length = 20)
    private String value;

    @Column(nullable = false)
    private boolean deleted;

    public Time(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public Time(String value) {
        this.value = value;
    }

    public Time() {

    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    @JsonIgnore
    public String getTime() {
        return value;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return deleted;
    }

    public void markDeleted() {
        deleted = true;
    }
}
