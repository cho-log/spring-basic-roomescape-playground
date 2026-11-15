package roomescape.waiting.repository;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.config.TestDataLoader;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;
import roomescape.waiting.WaitingWithRank;
import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ActiveProfiles("test")
@Import(TestDataLoader.class)
class WaitingRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private WaitingRepository waitingRepository;

    @Test
    void waitings_with_rank_are_loaded_in_one_query() {
        Member brown = entityManager.find(Member.class, 2L);
        Member charlie = entityManager.persist(
                new Member("찰리", "charlie@email.com", "password", "USER")
        );
        Theme theme = entityManager.find(Theme.class, 1L);
        Time time = entityManager.find(Time.class, 1L);

        entityManager.persist(new Waiting(brown, LocalDate.of(2024, 3, 1), time, theme));
        entityManager.persist(new Waiting(charlie, LocalDate.of(2024, 3, 1), time, theme));
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<WaitingWithRank> result = waitingRepository.findWaitingsWithRankByMemberId(charlie.getId());

        assertThat(result).singleElement().satisfies(waiting -> {
            assertThat(waiting.getTheme()).isEqualTo("테마1");
            assertThat(waiting.getTime()).isEqualTo(LocalTime.of(10, 0));
            assertThat(waiting.getRank()).isEqualTo(1L);
        });
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }
}
