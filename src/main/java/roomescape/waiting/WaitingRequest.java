package roomescape.waiting;

import jakarta.validation.constraints.NotNull;

public record WaitingRequest(
        @NotNull String date,
        @NotNull Long theme,
        @NotNull Long time
) {
}
