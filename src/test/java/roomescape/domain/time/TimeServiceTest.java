package roomescape.domain.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import roomescape.domain.time.entity.AvailableTime;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.service.TimeService;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;
import roomescape.global.exception.NotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TimeService.class, SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class TimeServiceTest {

    private final LocalTime value = LocalTime.of(3, 0).truncatedTo(ChronoUnit.MINUTES);

    @Autowired
    private TimeService timeService;

    @Test
    void 이미_저장된_값으로_save를_호출하면_예외를_던진다() {
        // then
        Assertions.assertThrows(
                DataIntegrityViolationException.class,

                // when
                () -> timeService.save(LocalTime.of(0, 0, 0))
        );
    }

    @Test
    void 정상적으로_time_레코드_저장에_성공한다() {
        // when
        Time savedTime = timeService.save(value);

        // then
        assertThat(savedTime).isNotNull();
        assertThat(savedTime.getId()).isNotNull();
        assertThat(savedTime.getTimeValue()).isEqualTo(value);
    }

    @Test
    void findAll_메소드를_호출하면_저장된_모든_Time을_반환한다() {
        // given
        timeService.save(value);

        // when
        List<Time> allTime = timeService.findAll();

        // then
        assertThat(allTime).hasSize(2);
    }

    @Test
    void getAvailableTime_메소드를_호출하면_해당하는_날짜와_테마의_예약_여부를_보여준다() {
        // when
        List<AvailableTime> availableTimes = timeService.getAvailableTime(LocalDate.of(2026, 9, 18), 1L);

        // then
        assertThat(availableTimes).hasSize(1);
        assertThat(availableTimes).filteredOn("booked", false).extracting(AvailableTime::getTime).containsExactly(LocalTime.of(0, 0));
    }

    @Test
    void 저장된_적_없는_ID로_deleteById를_호출하면_예외를_던진다() {
        Assertions.assertThrows(
                NotFoundException.class,
                // when
                () -> timeService.deleteById(-1L)
        );
    }

    @Test
    void 올바른_ID로_deleteById를_호출하면_해당_레코드가_삭제된다() {
        // given
        Time savedTime = timeService.save(value);

        // when
        timeService.deleteById(savedTime.getId());

        // then
        assertThat(timeService.findAll()).hasSize(1);
    }
}
