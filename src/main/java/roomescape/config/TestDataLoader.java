package roomescape.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

import java.util.List;

@Profile("test")
@Component
public class TestDataLoader implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final TimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public TestDataLoader(MemberRepository memberRepository, ThemeRepository themeRepository, TimeRepository timeRepository, ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Member admin = memberRepository.save(new Member("어드민", "admin@email.com", "password", Role.ADMIN));
        Member brown = memberRepository.save(new Member("브라운", "brown@email.com", "password", Role.USER));

        Theme firstTheme = themeRepository.save(new Theme("테마1", "테마1입니다."));
        Theme secondTheme = themeRepository.save(new Theme("테마2", "테마2입니다."));
        Theme thirdTheme = themeRepository.save(new Theme("테마3", "테마3입니다."));

        Time firstTime = timeRepository.save(new Time("10:00"));
        Time secondTime = timeRepository.save(new Time("12:00"));
        Time thirdTime = timeRepository.save(new Time("14:00"));
        timeRepository.save(new Time("16:00"));
        timeRepository.save(new Time("18:00"));
        timeRepository.save(new Time("20:00"));

        reservationRepository.saveAll(List.of(
                new Reservation(admin.getName(), "2024-03-01", firstTime, firstTheme, admin),
                new Reservation(admin.getName(), "2024-03-01", secondTime, secondTheme, admin),
                new Reservation(admin.getName(), "2024-03-01", thirdTime, thirdTheme, admin),
                new Reservation(brown.getName(), "2024-03-01", firstTime, secondTheme, brown)
        ));
    }
}
