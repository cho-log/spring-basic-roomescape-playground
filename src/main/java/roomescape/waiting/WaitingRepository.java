package roomescape.waiting;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, String date, Long timeId, Long themeId);

    long countByDateAndTimeIdAndThemeIdAndIdLessThan(String date, Long timeId, Long themeId, Long id);

    @EntityGraph(attributePaths = {"time", "theme"})
    @Query("""
            select new roomescape.waiting.WaitingWithRank(myWaiting,
                (select count(earlierWaiting) from Waiting earlierWaiting
                 where earlierWaiting.date = myWaiting.date
                   and earlierWaiting.time.id = myWaiting.time.id
                   and earlierWaiting.theme.id = myWaiting.theme.id
                   and earlierWaiting.id < myWaiting.id))
            from Waiting myWaiting
            where myWaiting.member.id = :memberId
            order by myWaiting.id
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(@Param("memberId") Long memberId);
}
