package roomescape.member;

import auth.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.UnauthorizedException;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    // 7단계) MemberService 대신 JwtUtils로 필드 수정
    private final JwtUtils jwtUtils;

    public AdminInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String url = request.getRequestURI();

        // GET 요청(조회)은 사용자도 호출 가능
        if ("GET".equalsIgnoreCase(request.getMethod()) && !url.startsWith("/admin")) {
            return true;
        }
        // 1) 요청에서 쿠키를 꺼내 토큰을 추출한다
        Cookie[] cookies = request.getCookies();
        String token = extractToken(cookies);

        // 7단계) DB조회 제거, Claims에서 추출한 role로 권한 확인
        Claims claims = jwtUtils.getClaims(token);

        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    private String extractToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        for (Cookie cookie : cookies) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        throw new UnauthorizedException("로그인이 필요합니다.");
    }
}
