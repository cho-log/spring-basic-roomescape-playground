package roomescape.domain.time.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.time.entity.Time;

import java.time.LocalTime;

public interface TimeRepository extends JpaRepository<Time,Long> {
    boolean existsByTimeValue(LocalTime timeValue);
}
