package roomescape.domain.waiting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.member.entity.Member;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.time.entity.Time;
import roomescape.domain.waiting.entity.ReserveWaiting;
import roomescape.domain.waiting.repository.ReserveWaitingRepository;
import roomescape.domain.waiting.service.result.WaitingWithRank;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class WaitingWithRankTest {

    @Autowired
    private ReserveWaitingRepository reserveWaitingRepository;

    private ReserveWaiting reserveWaiting = new ReserveWaiting(
            new Member("Alice", "alice@dummy.com", "dummy", "USER"),
            LocalDate.of(9999, 2, 1),
            new Time(LocalTime.of(11, 3)),
            new Theme("Dummy", "It is dummy for test")
    );

    @Test
    void WaitingWithRank는_ReserveWaiting이_빈_채로_생성할_수_없다() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new WaitingWithRank(null, 1L)
        );
    }

    @Test
    void WaitingWithRank는_rank가_빈_채로_생성할_수_없다() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new WaitingWithRank(reserveWaiting, null));
    }

    @Test
    void WaitingWithRank는_ID가_없는_ReserveWaiting으로_생성할_수_없다() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new WaitingWithRank(reserveWaiting, 1L)
        );
    }

    @Test
    void 정상적으로_WaitingWithRank를_생성() {
        // given

        ReserveWaiting reserveWaiting = findReserveWaiting(1L);
        // when
        WaitingWithRank waiting = new WaitingWithRank(reserveWaiting, 1L);

        // then
        assertThat(waiting).isNotNull();
        assertThat(waiting.reserveWaiting()).isNotNull();
        assertThat(waiting.reserveWaiting()).isEqualTo(reserveWaiting);
        assertThat(waiting.rank()).isEqualTo(1);
    }

    private ReserveWaiting findReserveWaiting(Long id) {
        return reserveWaitingRepository.findById(id).orElse(null);
    }
}
