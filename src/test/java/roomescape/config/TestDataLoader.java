package roomescape.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Profile("test")
@Component
public class TestDataLoader implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final TimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public TestDataLoader(MemberRepository memberRepository,
                          ThemeRepository themeRepository,
                          TimeRepository timeRepository,
                          ReservationRepository reservationRepository) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Member admin = memberRepository.save(
                new Member("어드민", "admin@email.com", "password", "ADMIN")
        );
        memberRepository.save(new Member("브라운", "brown@email.com", "password", "USER"));

        List<Theme> themes = new ArrayList<>();
        for (int index = 1; index <= 3; index++) {
            themes.add(themeRepository.save(new Theme("테마" + index, "테마" + index + "입니다.")));
        }
        List<Time> times = new ArrayList<>();
        for (int hour = 10; hour <= 20; hour += 2) {
            times.add(timeRepository.save(new Time(LocalTime.of(hour, 0))));
        }

        LocalDate date = LocalDate.of(2024, 3, 1);
        for (int index = 0; index < 3; index++) {
            reservationRepository.save(Reservation.byMember(admin, date, times.get(index), themes.get(index)));
        }
        reservationRepository.save(Reservation.byName("브라운", date, times.get(0), themes.get(1)));
    }
}
