package roomescape.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import roomescape.exception.InvalidRequestException;
import roomescape.member.Member;
import roomescape.theme.Theme;
import roomescape.time.Time;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private Time time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    protected Reservation() {
    }

    public Reservation(Long id, String name, String date, Time time, Theme theme, Member member) {
        validateName(name);
        validateDate(date);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(String name, String date, Time time, Theme theme, Member member) {
        this(null, name, date, time, theme, member);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("예약자 이름은 비어있을 수 없습니다.");
        }
    }

    private void validateDate(String date) {
        if (date == null || date.isBlank()) {
            throw new InvalidRequestException("예약 날짜는 비어있을 수 없습니다.");
        }
    }

    // 예약이 본인 것인지 스스로 확인하도록 추가
    // 7단계) Member 객체 대신 memberId를 받음(id로 확인 가능)
    public boolean isOwnedBy(Long memberId) {
        if (this.member == null) {
            return false;
        }
        return this.member.getId().equals(memberId);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }
}
