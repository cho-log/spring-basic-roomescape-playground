package roomescape.reservation.domain;

import jakarta.persistence.*;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_date_time_theme",
                columnNames = {"date", "time_id", "theme_id"}
        )
)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String date;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private Time time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public Reservation(String name, String date, Time time, Theme theme, Member member) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation() {

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

    public static Reservation create(
            String name,
            String date,
            Time time,
            Theme theme,
            Member member
    ) {
        validateFutureDateTime(date, time);

        return new Reservation(name, date, time, theme, member);
    }

    private static void validateFutureDateTime(String date, Time time) {
        try {
            LocalDateTime reservationDateTime = LocalDateTime.of(
                    LocalDate.parse(date),
                    LocalTime.parse(time.getValue())
            );

            if (!reservationDateTime.isAfter(LocalDateTime.now())) {
                throw new InvalidReservationException(
                        "올바른 예약 날짜와 시간을 선택해야 합니다."
                );
            }
        } catch (DateTimeParseException exception) {
            throw new InvalidReservationException(
                    "올바른 예약 날짜와 시간을 선택해야 합니다."
            );
        }
    }
}
