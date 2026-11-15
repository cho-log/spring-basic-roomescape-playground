package roomescape.member;

import auth.LoginMember;

public record LoginCheckResponse(
        String name
) {
    public static LoginCheckResponse from(LoginMember loginMember) {
        return new LoginCheckResponse(loginMember.name());
    }
}
