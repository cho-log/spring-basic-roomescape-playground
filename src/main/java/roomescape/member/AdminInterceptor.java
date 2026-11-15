package roomescape.member;

import auth.JwtUtils;
import auth.TokenPayload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

public class AdminInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public AdminInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = TokenExtractor.extract(request.getCookies());
        TokenPayload payload = jwtUtils.parse(token);

        if (Role.valueOf(payload.getRole()) != Role.ADMIN) {
            throw new BusinessException(ErrorCode.NOT_ADMIN);
        }
        return true;
    }
}
