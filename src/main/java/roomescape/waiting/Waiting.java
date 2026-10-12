package roomescape.waiting;

import jakarta.persistence.*;
import roomescape.theme.Theme;
import roomescape.time.Time;

import java.time.LocalDate;

@Entity
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long memberId;
    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER)
    private Time time;

    @ManyToOne(fetch = FetchType.EAGER)
    private Theme theme;

    public Waiting() {}

    public Waiting(Long memberId, LocalDate date, Time time, Theme theme) {
        this.memberId = memberId;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}