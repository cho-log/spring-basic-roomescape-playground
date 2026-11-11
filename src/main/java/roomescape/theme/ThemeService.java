package roomescape.theme;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<Theme> findAll() {
        return themeRepository.findAllByDeletedFalseOrderByIdAsc();
    }

    @Transactional
    public Theme save(Theme theme) {
        return themeRepository.save(new Theme(theme.getName(), theme.getDescription()));
    }

    @Transactional
    public void deleteById(Long id) {
        themeRepository.findById(id).ifPresent(Theme::markDeleted);
    }
}
