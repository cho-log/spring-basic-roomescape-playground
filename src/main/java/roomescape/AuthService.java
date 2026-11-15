package roomescape;

import auth.JwtUtils;
import auth.LoginMember;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import roomescape.member.AuthorizationException;
import roomescape.member.Member;
import roomescape.member.MemberService;

@Service
public class AuthService {
    private final CookieUtil cookieUtil;
    private final JwtUtils jwtUtils;
    private final MemberService memberService;

    public AuthService(CookieUtil cookieUtil, JwtUtils jwtUtils, MemberService memberService) {
        this.cookieUtil = cookieUtil;
        this.jwtUtils = jwtUtils;
        this.memberService = memberService;
    }

    public LoginMember findLoginMember(HttpServletRequest request) {
        String token = cookieUtil.extractToken(request);
        if (token.isEmpty()) {
            throw new AuthorizationException("쿠키에 토큰이 없습니다.");
        }
        return jwtUtils.getLoginMember(token);
    }

    public Member findAuthenticatedMember(HttpServletRequest request) {
        LoginMember loginMember = findLoginMember(request);
        return memberService.findById(loginMember.id());
    }
}
