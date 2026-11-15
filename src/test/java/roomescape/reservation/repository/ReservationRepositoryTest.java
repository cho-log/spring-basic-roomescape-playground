package roomescape.reservation.repository;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.config.TestDataLoader;
import roomescape.reservation.domain.Reservation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@ActiveProfiles("test")
@Import(TestDataLoader.class)
class ReservationRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void reservations_with_theme_and_time_are_loaded_in_one_query() {
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<Reservation> reservations = reservationRepository.findAllWithThemeAndTime();
        reservations.forEach(reservation -> {
            reservation.getTheme().getName();
            reservation.getTime().getValue();
        });

        assertThat(reservations).isNotEmpty();
        assertThat(reservations)
                .filteredOn(reservation -> reservation.getMember() != null)
                .allMatch(reservation -> !Hibernate.isInitialized(reservation.getMember()));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }

    @Test
    void member_reservations_with_theme_and_time_are_loaded_in_one_query() {
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        List<Reservation> reservations = reservationRepository.findByMember_IdOrderByIdAsc(1L);
        reservations.forEach(reservation -> {
            reservation.getTheme().getName();
            reservation.getTime().getValue();
        });

        assertThat(reservations).isNotEmpty();
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1L);
    }
}
