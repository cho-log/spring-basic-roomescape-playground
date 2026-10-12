package roomescape.auth;

import auth.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

@Component
public class LoginMemberResolver {

    private final JwtUtils jwtUtils;

    public LoginMemberResolver(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public LoginMember resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        String token = extractTokenFromCookie(cookies);
        if (token.isBlank()) {
            return null;
        }

        try {
            Claims claims = jwtUtils.getClaims(token);

            return new LoginMember(
                    Long.valueOf(claims.getSubject()),
                    claims.get("name", String.class),
                    claims.get("role", String.class));
        } catch (JwtException e) {
            return null;
        }
    }

    private String extractTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        return "";
    }
}