package roomescape.domain.theme.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ThemeRequest(
        @NotBlank(message = "이름 필드는 필수값입니다.")
        @Size(min = 1, max = 255, message = "이름 필드는 1자 이상, 255자 이하여야 합니다.")
        String name,

        @NotBlank(message = "설명 필드는 필수값입니다.")
        @Size(min = 1, max = 255, message = "설명 필드는 1자 이상, 255자 이하여야 합니다.")
        String description
) {
}
