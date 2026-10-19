package roomescape.waiting;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.Member;
import roomescape.theme.Theme;
import roomescape.time.Time;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByMemberId(Long memberId);

    boolean existsByMemberAndDateAndTimeAndTheme(
        Member member,
        String date,
        Time time,
        Theme theme
    );

    @Query("SELECT new roomescape.waiting.WaitingWithRank(" +
        "    w, " +
        "    (SELECT COUNT(w2) " +
        "     FROM Waiting w2 " +
        "     WHERE w2.theme = w.theme " +
        "       AND w2.date = w.date " +
        "       AND w2.time = w.time " +
        "       AND w2.id < w.id)) " +
        "FROM Waiting w " +
        "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

}
