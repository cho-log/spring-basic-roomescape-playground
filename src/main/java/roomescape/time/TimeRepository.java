package roomescape.time;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeRepository extends JpaRepository<Time, Long> {

    List<Time> findAllByDeletedFalse();
}
