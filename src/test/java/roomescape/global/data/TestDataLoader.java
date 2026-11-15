package roomescape.global.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.domain.waiting.entity.ReserveWaiting;
import roomescape.domain.waiting.repository.ReserveWaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class TestDataLoader implements CommandLineRunner {

    private MemberRepository memberRepository;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;
    private ReserveWaitingRepository reserveWaitingRepository;

    public TestDataLoader(MemberRepository memberRepository, TimeRepository timeRepository, ThemeRepository themeRepository, ReservationRepository reservationRepository, ReserveWaitingRepository reserveWaitingRepository) {
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.reserveWaitingRepository = reserveWaitingRepository;
    }

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        memberRepository.save(new Member("더미_어드민", "admin@dummy.com", "dummy", "ADMIN"));
        Member user = memberRepository.save(new Member("더미_유저", "user@dummy.com", "dummy", "USER"));

        Time time = timeRepository.save(new Time(LocalTime.of(0, 0)));

        Theme theme = themeRepository.save(new Theme("dummy", "dummy"));

        reservationRepository.save(new Reservation(LocalDate.of(9999, 12, 31), user, time, theme));

        reserveWaitingRepository.save(new ReserveWaiting(user, LocalDate.of(9999, 1, 1), time, theme));
    }
}
