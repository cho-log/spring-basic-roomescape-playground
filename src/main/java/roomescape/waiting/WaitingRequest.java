package roomescape.waiting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WaitingRequest {
    @NotBlank
    private String date;

    @NotNull
    private Long time;

    @NotNull
    private Long theme;

    public String getDate() {
        return date;
    }

    public Long getTime() {
        return time;
    }

    public Long getTheme() {
        return theme;
    }
}
