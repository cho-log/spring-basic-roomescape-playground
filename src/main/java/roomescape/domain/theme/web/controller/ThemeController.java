package roomescape.domain.theme.web.controller;

import auth.principal.LoginMember;
import auth.support.annotation.AdminOnly;
import auth.support.annotation.Login;
import auth.support.annotation.Public;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.service.ThemeService;
import roomescape.domain.theme.web.dto.ThemeRequest;
import roomescape.domain.theme.web.dto.ThemeResponse;
import roomescape.global.exception.BadRequestException;
import roomescape.global.exception.ConflictException;

import java.net.URI;
import java.util.List;

@RestController
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @AdminOnly
    @PostMapping("/themes")
    public ResponseEntity<ThemeResponse> createTheme(
            @Valid @RequestBody ThemeRequest request,
            @Login LoginMember loginMember
    ) {
        Theme newTheme;

        try {
            newTheme = themeService.saveTheme(request.name(), request.description());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("이미 존재하는 테마 이름입니다.");
        }

        return ResponseEntity.created(URI.create("/themes/" + newTheme.getId())).body(ThemeResponse.from(newTheme));
    }

    @Public
    @GetMapping("/themes")
    public List<ThemeResponse> list() {
        return themeService.findAllTheme().stream().map(ThemeResponse::from).toList();
    }

    @AdminOnly
    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(
            @PathVariable(name = "id") Long themeId
    ) {
        try {
            themeService.deleteTheme(themeId);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("해당 테마로 예약 혹은 예약 대기된 건이 있습니다. 해당 건을 삭제한 후 다시 시도하여 주세요.");
        } catch (OptimisticLockingFailureException e) {
            // 삭제 동시 요청의 경우, 이미 삭제된 리소스에 대한 추가 삭제는 예외 반환이 필요 없다 판단.
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.noContent().build();
    }
}
