package roomescape.time;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeRepository extends JpaRepository<Time, Long> {
    List<Time> findAllByDeletedFalseOrderByIdAsc();
}
