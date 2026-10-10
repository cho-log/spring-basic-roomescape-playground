package roomescape.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;

import java.util.List;

@Profile("test") // test 프로파일(테스트 환경)에서만 등록되어 실행
@Component
public class TestDataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final TimeRepository timeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataLoader(MemberRepository memberRepository,
                          ThemeRepository themeRepository,
                          TimeRepository timeRepository,
                          ReservationRepository reservationRepository,
                          WaitingRepository waitingRepository,
                          PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String password = passwordEncoder.encode("password");

        // 1. 회원 저장
        Member admin = memberRepository.save(new Member("어드민", "admin@email.com", password, "ADMIN"));
        Member brown = memberRepository.save(new Member("브라운", "brown@email.com", password, "USER"));

        // 2. 테마 및 시간 저장
        Theme theme1 = themeRepository.save(new Theme("테마1", "테마1입니다."));
        Theme theme2 = themeRepository.save(new Theme("테마2", "테마2입니다."));
        Theme theme3 = themeRepository.save(new Theme("테마3", "테마3입니다."));

        List<Time> times = timeRepository.saveAll(List.of(
                new Time("10:00"), new Time("12:00"), new Time("14:00"),
                new Time("16:00"), new Time("18:00"), new Time("20:00")
        ));

        // 3. 예약 및 대기 저장
        reservationRepository.save(new Reservation("어드민", "2024-03-01", times.get(0), theme1, admin));
        reservationRepository.save(new Reservation("어드민", "2024-03-01", times.get(1), theme2, admin));
        reservationRepository.save(new Reservation("어드민", "2024-03-01", times.get(2), theme3, admin));
        reservationRepository.save(new Reservation("브라운", "2024-03-01", times.get(0), theme2, null));

        waitingRepository.save(new Waiting("2024-03-01", times.get(0), theme2, brown.getId()));
    }
}
