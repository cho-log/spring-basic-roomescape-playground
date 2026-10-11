package roomescape;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;

@Profile("test")
@Component
@Order(2)
public class TestDataLoader implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public TestDataLoader(MemberRepository memberRepository, TimeRepository timeRepository,
                          ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) {
        Time time1 = timeRepository.save(new Time(LocalTime.of(10, 0)));
        Time time2 = timeRepository.save(new Time(LocalTime.of(12, 0)));
        Time time3 = timeRepository.save(new Time(LocalTime.of(14, 0)));

        Theme theme1 = themeRepository.save(new Theme("테마1", "설명1"));
        Theme theme2 = themeRepository.save(new Theme("테마2", "설명2"));
        Theme theme3 = themeRepository.save(new Theme("테마3", "설명3"));

        Member admin = memberRepository.findById(1L).orElseThrow();
        LocalDate date = LocalDate.of(2024, 3, 1);

        reservationRepository.save(new Reservation(admin, date, time1, theme1));
        reservationRepository.save(new Reservation(admin, date, time2, theme2));
        reservationRepository.save(new Reservation(admin, date, time3, theme3));
        reservationRepository.save(new Reservation("브라운", date, time1, theme2));
    }
}