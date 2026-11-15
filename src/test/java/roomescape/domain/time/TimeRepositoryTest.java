package roomescape.domain.time;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class TimeRepositoryTest {

    private final LocalTime value = LocalTime.of(11, 0);

    @Autowired
    private TimeRepository timeRepository;

    @Test
    void save를_호출하면_ID가_있는_객체를_반환한다() {
        // given
        Time time = new Time(value);

        // when
        Time savedTime = timeRepository.save(time);

        // then
        assertThat(time).isSameAs(savedTime);
        assertThat(savedTime.getId()).isNotNull();

        assertThat(savedTime.getTimeValue()).isEqualTo(value);
    }

    @Test
    void findAll을_호출하면_저장된_모든_시간을_반환한다() {
        // given
        timeRepository.save(new Time(LocalTime.of(10, 0)));
        timeRepository.save(new Time(LocalTime.of(12, 0)));

        // when & then
        assertThat(timeRepository.findAll()).hasSize(3);
    }

    @Test
    void deleteById를_호출하면_해당_시간이_조회에서_제외된다() {
        // given
        Time savedTime = timeRepository.save(new Time(value));

        // when
        timeRepository.deleteById(savedTime.getId());

        // then
        assertThat(timeRepository.findAll()).hasSize(1);
    }
}
