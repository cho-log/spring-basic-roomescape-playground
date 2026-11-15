package roomescape.member.interceptor;

import auth.LoginMember;
import auth.MemberSessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.exception.MemberException;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final MemberSessionManager memberSessionManager;

    public AdminInterceptor(MemberSessionManager memberSessionManager) {
        this.memberSessionManager = memberSessionManager;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        LoginMember loginMember = memberSessionManager.findLoginMember(request)
                .orElseThrow(() -> new MemberException(MemberErrorCode.LOGIN_REQUIRED));

        if (!loginMember.isAdmin()) {
            throw new MemberException(MemberErrorCode.ADMIN_REQUIRED);
        }

        return true;
    }

}
