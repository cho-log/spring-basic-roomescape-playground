package roomescape;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class JpaTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TimeRepository timeRepository;

    @Test
    void 사단계() {
        Time time = new Time("22:00");
        entityManager.persist(time);
        entityManager.flush();
        entityManager.clear();

        Time persistTime = timeRepository.findById(time.getId()).orElse(null);

        assertThat(persistTime.getValue()).isEqualTo(time.getValue());
    }

    @Value("${roomescape.auth.jwt.secret}")
    private String secretKey;

    @Test
    void 팔단계() {
        assertThat(secretKey).isNotBlank();
    }
}
