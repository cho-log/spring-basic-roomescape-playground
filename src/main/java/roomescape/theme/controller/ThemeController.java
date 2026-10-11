package roomescape.theme.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.exception.auth.ForbiddenAdminOperationException;
import roomescape.exception.theme.InvalidThemeException;
import roomescape.exception.theme.NotFoundThemeException;
import roomescape.member.domain.Role;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

import java.net.URI;
import java.util.List;

@RestController
public class ThemeController {
    private static final int MAX_THEME_LENGTH = 255;
    private final ThemeRepository themeRepository;

    public ThemeController(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @PostMapping("/themes")
    public ResponseEntity<Theme> createTheme(LoginMember loginMember, @RequestBody Theme theme) {
        if (loginMember.role() != Role.ADMIN) {
            throw new ForbiddenAdminOperationException("관리자만 테마를 관리할 수 있습니다.");
        }

        if (theme.getName() == null
                || theme.getName().isBlank()
                || theme.getName().length() > MAX_THEME_LENGTH
                || theme.getDescription() == null
                || theme.getDescription().isBlank()
                || theme.getDescription().length() > MAX_THEME_LENGTH) {
            throw new InvalidThemeException("테마 정보를 올바르게 입력해야 합니다.");
        }
        Theme newTheme = themeRepository.save(theme);
        return ResponseEntity.created(URI.create("/themes/" + newTheme.getId())).body(newTheme);
    }

    @GetMapping("/themes")
    public ResponseEntity<List<Theme>> list() {
        return ResponseEntity.ok(themeRepository.findAllByDeletedFalse());
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(LoginMember loginMember, @PathVariable Long id) {
        if (loginMember.role() != Role.ADMIN) {
            throw new ForbiddenAdminOperationException("관리자만 테마를 관리할 수 있습니다.");
        }

        Theme theme = themeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundThemeException("삭제할 테마를 찾을 수 없습니다."));

        theme.delete();
        themeRepository.save(theme);

        return ResponseEntity.noContent().build();
    }
}
