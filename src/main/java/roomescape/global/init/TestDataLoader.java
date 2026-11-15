package roomescape.global.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.slot.Slot;
import roomescape.slot.SlotRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

@Component
@Profile("test")
public class TestDataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final TimeRepository timeRepository;
    private final SlotRepository slotRepository;
    private final ReservationRepository reservationRepository;

    public TestDataLoader(
            MemberRepository memberRepository,
            ThemeRepository themeRepository,
            TimeRepository timeRepository,
            SlotRepository slotRepository,
            ReservationRepository reservationRepository
    ) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Member admin = memberRepository.save(
                new Member("어드민", "admin@email.com", "password", "ADMIN"));
        Member brown = memberRepository.save(
                new Member("브라운", "brown@email.com", "password", "USER"));

        Theme theme1 = themeRepository.save(new Theme("테마1", "테마1입니다."));
        Theme theme2 = themeRepository.save(new Theme("테마2", "테마2입니다."));
        Theme theme3 = themeRepository.save(new Theme("테마3", "테마3입니다."));

        Time time1 = timeRepository.save(new Time("10:00"));
        Time time2 = timeRepository.save(new Time("12:00"));
        Time time3 = timeRepository.save(new Time("14:00"));
        timeRepository.save(new Time("16:00"));
        timeRepository.save(new Time("18:00"));
        timeRepository.save(new Time("20:00"));

        Slot slot1 = slotRepository.save(new Slot("2024-03-01", time1, theme1));
        Slot slot2 = slotRepository.save(new Slot("2024-03-01", time2, theme2));
        Slot slot3 = slotRepository.save(new Slot("2024-03-01", time3, theme3));
        Slot slot4 = slotRepository.save(new Slot("2024-03-01", time1, theme2));

        reservationRepository.save(new Reservation("", slot1, admin));
        reservationRepository.save(new Reservation("", slot2, admin));
        reservationRepository.save(new Reservation("", slot3, admin));
        reservationRepository.save(new Reservation("브라운", slot4, brown));
    }
}
