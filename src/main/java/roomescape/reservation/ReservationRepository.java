package roomescape.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.Theme;
import roomescape.time.Time;

import java.time.LocalDate;
import java.util.List;


public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeAndDateAndTime(Theme theme, LocalDate date, Time time);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.time " +
            "JOIN FETCH r.theme " +
            "LEFT JOIN FETCH r.member")
    List<Reservation> findAllWithDetails();

    @Query("SELECT r FROM Reservation r " +
            "JOIN FETCH r.time " +
            "JOIN FETCH r.theme " +
            "WHERE r.member.id = :memberId")
    List<Reservation> findByMemberIdWithDetails(Long memberId);
}
