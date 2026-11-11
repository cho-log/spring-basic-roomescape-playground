package roomescape.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByDateAndTimeIdAndThemeId(String date, Long timeId, Long themeId);

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, String date, Long timeId, Long themeId);

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findByMemberIdOrderByIdAsc(Long memberId);

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findByDateAndThemeId(String date, Long themeId);
}
