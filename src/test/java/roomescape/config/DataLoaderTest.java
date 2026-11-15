package roomescape.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.repository.TimeRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("production")
@DirtiesContext
class DataLoaderTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void production_initializes_only_members() {
        assertThat(context.getBeansOfType(DataLoader.class)).hasSize(1);
        assertThat(context.getBeansOfType(TestDataLoader.class)).isEmpty();
        assertThat(memberRepository.count()).isEqualTo(2);
        assertThat(themeRepository.count()).isZero();
        assertThat(timeRepository.count()).isZero();
        assertThat(reservationRepository.count()).isZero();

        context.getBean(DataLoader.class).run();

        assertThat(memberRepository.count()).isEqualTo(2);
    }
}
