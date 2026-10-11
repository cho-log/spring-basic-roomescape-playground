package roomescape.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

@Profile("test")
@Component
public class TestDataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final TimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public TestDataLoader(MemberRepository memberRepository, ThemeRepository themeRepository,
                          TimeRepository timeRepository, ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) {
        Member admin = memberRepository.findById(1L).orElseThrow();

        Theme theme1 = themeRepository.save(new Theme("테마1", "테마1입니다."));
        Theme theme2 = themeRepository.save(new Theme("테마2", "테마2입니다."));
        Theme theme3 = themeRepository.save(new Theme("테마3", "테마3입니다."));

        Time time1 = timeRepository.save(new Time("10:00"));
        Time time2 = timeRepository.save(new Time("12:00"));
        Time time3 = timeRepository.save(new Time("14:00"));
        timeRepository.save(new Time("16:00"));
        timeRepository.save(new Time("18:00"));
        timeRepository.save(new Time("20:00"));

        reservationRepository.save(new Reservation(admin, "2024-03-01", time1, theme1));
        reservationRepository.save(new Reservation(admin, "2024-03-01", time2, theme2));
        reservationRepository.save(new Reservation(admin, "2024-03-01", time3, theme3));
        reservationRepository.save(new Reservation("브라운", "2024-03-01", time1, theme2));
    }
}
