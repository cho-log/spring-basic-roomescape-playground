package roomescape.domain.theme.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.global.exception.NotFoundException;

import java.util.List;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeService(
            ThemeRepository themeRepository
    ) {
        this.themeRepository = themeRepository;
    }

    @Transactional
    public Theme saveTheme(String name, String description) {
        Theme theme = new Theme(name, description);

        return themeRepository.save(theme);
    }

    public List<Theme> findAllTheme() {
        return themeRepository.findAll();
    }

    @Transactional
    public void deleteTheme(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("해당하는 테마를 찾을 수 없습니다."));

        themeRepository.delete(theme);
    }
}
