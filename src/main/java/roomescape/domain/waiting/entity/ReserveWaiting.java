package roomescape.domain.waiting.entity;

import jakarta.persistence.*;
import roomescape.domain.member.entity.Member;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;

import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_reserve_waiting_member_date_time_theme",
        columnNames = {
                "date", "theme_id", "time_id", "member_id"
        })
)
public class ReserveWaiting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reserve_waiting_time"))
    private Time time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reserve_waiting_theme"))
    private Theme theme;

    protected ReserveWaiting() {}

    public ReserveWaiting(Member member, LocalDate date, Time time, Theme theme) {
        validateFields(member, date, time, theme);
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

    private void validateFields(Member member, LocalDate date, Time time, Theme theme) {
        if (member == null) {
            throw new IllegalArgumentException("ReserveWaiting을 생성하기 위해 member는 필수 필드입니다.");
        }

        if (date == null) {
            throw new IllegalArgumentException("ReserveWaiting을 생성하기 위해 date는 필수 필드입니다.");
        }

        if (time == null) {
            throw new IllegalArgumentException("ReserveWaiting을 생성하기 위해 time은 필수 필드입니다.");
        }

        if (theme == null) {
            throw new IllegalArgumentException("ReserveWaiting을 생성하기 위해 theme는 필수 필드입니다.");
        }
    }
}
