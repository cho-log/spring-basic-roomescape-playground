package roomescape.domain.waiting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.waiting.service.ReserveWaitingService;
import roomescape.domain.waiting.service.result.WaitingWithRank;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.NotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ReserveWaitingService.class, SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class ReserveWaitingServiceTest {

    private final Long memberId = 2L;
    private final LocalDate date = LocalDate.of(9999, 1, 1);
    private final Long timeId = 1L;
    private final Long themeId = 1L;

    @Autowired
    private ReserveWaitingService reserveWaitingService;

    @Test
    void createReserveWaiting을_존재하지_않는_memberId로_호출하면_예외가_발생한다() {
        // then
        Assertions.assertThrows(
                NotFoundException.class,

                // when
                () -> reserveWaitingService.createReserveWaiting(-1L, date, timeId, themeId)
        );
    }

    @Test
    void createReserveWaiting을_존재하지_않는_timeId로_호출하면_예외가_발생한다() {
        // then
        Assertions.assertThrows(
                DataIntegrityViolationException.class,

                // when
                () -> reserveWaitingService.createReserveWaiting(memberId, date, -1L, themeId)
        );
    }

    @Test
    void createReserveWaiting을_존재하지_않는_themeId로_호출하면_예외가_발생한다() {
        // then
        Assertions.assertThrows(
                DataIntegrityViolationException.class,

                // when
                () -> reserveWaitingService.createReserveWaiting(memberId, date, timeId, -1L)
        );
    }

    @Test
    void 이미_예약_중인_조합으로_createReserveWaiting을_호출하면_예외가_발생한다() {
        // then
        Assertions.assertThrows(
                BadRequestException.class,

                // when
                () -> reserveWaitingService.createReserveWaiting(memberId, LocalDate.of(9999, 12, 31), timeId, themeId)
        );
    }

    @Test
    void 이미_예약_대기한_조합으로_createReserveWaiting을_호출하면_예외가_발생한다() {
        // then
        Assertions.assertThrows(
                DataIntegrityViolationException.class,
                () -> reserveWaitingService.createReserveWaiting(memberId, date, timeId, themeId)
        );
    }

    @Test
    void 정상적으로_createReserveWaiting을_호출하면_예약된_ReserveWaiting과_순번을_갖는_WaitingWithRank를_반환한다() {
        // when
        WaitingWithRank waiting = reserveWaitingService.createReserveWaiting(memberId, date.plusDays(1), timeId, themeId);

        // then
        assertThat(waiting).isNotNull();
        assertThat(waiting.reserveWaiting()).isNotNull();
        assertThat(waiting.reserveWaiting().getId()).isNotNull();
        assertThat(waiting.reserveWaiting().getMember()).isNotNull();
        assertThat(waiting.reserveWaiting().getDate()).isEqualTo(date.plusDays(1));
        assertThat(waiting.reserveWaiting().getTime()).isNotNull();
        assertThat(waiting.reserveWaiting().getTheme()).isNotNull();
    }

    @Test
    void 존재하지_않는_ReserveWaitingId로_deleteReserveWaiting을_호출_시_예외가_발생한다() {
        // when
        Assertions.assertThrows(
                NotFoundException.class,
                () -> reserveWaitingService.deleteReserveWaiting(memberId, 2L)
        );
    }

    @Test
    void 존재하지만_해당_멤버의_소유가_아닌_경우에_deleteReserveWaiting을_호출_시_예외가_발생한다() {
        // when
        Assertions.assertThrows(
                NotFoundException.class,
                () -> reserveWaitingService.deleteReserveWaiting(1L, 1L)
        );
    }

    @Test
    void 정상적으로_deleteReserveWaiting을_호출() {
        // when
        reserveWaitingService.deleteReserveWaiting(memberId, 1L);

        // then
        List<WaitingWithRank> membersAllReserveWaiting = reserveWaitingService.findAllMemberReserveWaits(memberId);
        assertThat(membersAllReserveWaiting).isEmpty();
    }
}
