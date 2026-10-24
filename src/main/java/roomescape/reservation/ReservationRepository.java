package roomescape.reservation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.Member;
import roomescape.theme.Theme;
import roomescape.time.Time;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByDateAndThemeId(String date, Long themeId);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findByMemberId(Long memberId);

    boolean existsByMemberAndDateAndThemeAndTime(Member member, String date, Theme theme, Time time);

    boolean existsByDateAndThemeAndTime(String date, Theme theme, Time time);
}
