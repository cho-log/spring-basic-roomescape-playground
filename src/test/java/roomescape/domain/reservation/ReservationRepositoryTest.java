package roomescape.domain.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class ReservationRepositoryTest {

    private final String alice = "Alice";
    private final String bob = "Bob";
    private final LocalDate date = LocalDate.now().plusDays(1);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void save를_호출하면_ID가_있는_객체를_반환한다() {
        // given
        Time time = saveTime();
        Theme theme = saveTheme();
        Member member = saveMember(alice);

        // when
        Reservation savedReservation = reservationRepository.save(new Reservation(date, member, time, theme));

        // then
        assertThat(savedReservation.getId()).isNotNull();
        assertThat(savedReservation.getMember().getNickname()).isEqualTo(alice);
        assertThat(savedReservation.getDate()).isEqualTo(date);
        assertThat(savedReservation.getTime().getId()).isEqualTo(time.getId());
        assertThat(savedReservation.getTheme().getId()).isEqualTo(theme.getId());
    }

    @Test
    void findAll을_호출하면_저장된_모든_예약을_반환한다() {
        // given
        Time time = saveTime();
        Theme theme = saveTheme();

        reservationRepository.save(new Reservation(date, saveMember(alice), time, theme));
        reservationRepository.save(new Reservation(date.plusDays(1), saveMember(bob), time, theme));

        // when & then
        assertThat(reservationRepository.findAll()).hasSize(3);
    }

    @Test
    void deleteById를_호출하면_해당_예약이_삭제된다() {
        // given
        Time time = saveTime();
        Theme theme = saveTheme();
        Reservation savedReservation = reservationRepository.save(new Reservation(date, saveMember(alice), time, theme));

        // when
        reservationRepository.deleteById(savedReservation.getId());

        // then
        assertThat(reservationRepository.findAll()).hasSize(1);
    }

    @Test
    void findByDateAndThemeId를_호출하면_날짜와_테마가_일치하는_예약만_반환한다() {
        // given
        Time time = saveTime();
        Theme theme = saveTheme();

        reservationRepository.save(new Reservation(date, saveMember(alice), time, theme));

        // when
        List<Reservation> reservations = reservationRepository.findByDateAndThemeId(date, theme.getId());

        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getMember().getNickname()).isEqualTo(alice);
    }

    private Time saveTime() {
        return timeRepository.save(new Time(LocalTime.of(3, 0)));
    }

    private Theme saveTheme() {
        return themeRepository.save(new Theme("Dummy", "it is Dummy for Test"));
    }

    private Member saveMember(String nickname) {
        return memberRepository.save(new Member(nickname, nickname.toLowerCase() + "@dummy.com", "dummy", "USER"));
    }
}
