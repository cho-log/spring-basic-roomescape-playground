package roomescape.theme;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    List<Theme> findAllByDeletedFalseOrderByIdAsc();
}
