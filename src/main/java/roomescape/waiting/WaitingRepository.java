package roomescape.waiting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.Member;
import roomescape.theme.Theme;
import roomescape.time.Time;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
        SELECT w,
               (SELECT COUNT(w2)
                FROM Waiting w2
                WHERE w2.theme = w.theme
                  AND w2.date = w.date
                  AND w2.time = w.time
                  AND w2.id < w.id)
        FROM Waiting w
        JOIN FETCH w.theme
        JOIN FETCH w.time
        WHERE w.member.id = :memberId
        """)
    List<Object[]> findWaitingsWithRankRawByMemberId(Long memberId);

    default List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId) {
        return findWaitingsWithRankRawByMemberId(memberId).stream()
                .map(result -> new WaitingWithRank(
                        (Waiting) result[0],
                        (Long) result[1]
                ))
                .toList();
    }

    @Query(" SELECT COUNT(w) " +
            " FROM Waiting w " +
            " WHERE w.theme = :theme " +
            "   AND w.date = :date " +
            "   AND w.time = :time " +
            "   AND w.id < :waitingId ")
    long countEarlierWaitings(Theme theme, String date, Time time, Long waitingId);

    boolean existsByMemberAndDateAndThemeAndTime(Member member, String date, Theme theme, Time time);
}
