package roomescape.domain.reservation.entity;

import jakarta.persistence.*;
import roomescape.domain.member.entity.Member;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

import java.time.LocalDate;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_date_time_theme",
                columnNames = {
                "date", "time_id", "theme_id"
        })
)
public class Reservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_time"))
    private Time time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_theme"))
    private Theme theme;

    protected Reservation() {
    }

    public Reservation(LocalDate date, Member member, Time time, Theme theme) {
        validateFields(date, member, time, theme);
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
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

    private void validateFields(LocalDate date, Member member, Time time, Theme theme) {
        if (date == null) {
            throw new IllegalArgumentException("Reservation을 만들기 위해 date는 필수 필드입니다.");
        }

        if (member == null) {
            throw new IllegalArgumentException("Reservation을 만들기 위해 member는 필수 필드입니다.");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Reservation은 현재, 혹은 과거의 날짜로 생성할 수 없습니다.");
        }

        if (time == null) {
            throw new IllegalArgumentException("Reservation을 만들기 위해 time은 필수 필드입니다.");
        }

        if (theme == null) {
            throw new IllegalArgumentException("Reservation을 만들기 위해 theme는 필수 필드입니다.");
        }
    }
}
