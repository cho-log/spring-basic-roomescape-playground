package roomescape.auth;

import auth.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginMemberResolverTest {

    private final JwtUtils jwtUtils = new JwtUtils();
    private final LoginMemberResolver loginMemberResolver = new LoginMemberResolver(jwtUtils);

    @Test
    void 형식이_깨진_토큰이면_null을_반환한다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("token", "not-a-jwt")});

        LoginMember loginMember = loginMemberResolver.resolve(request);

        assertThat(loginMember).isNull();
    }

    @Test
    void 서명이_일치하지_않는_토큰이면_null을_반환한다() {
        String wrongKeySignedToken = Jwts.builder()
                .setSubject("1")
                .claim("name", "어드민")
                .claim("role", "ADMIN")
                .signWith(Keys.hmacShaKeyFor("this-is-a-completely-different-secret-key-value".getBytes()))
                .compact();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("token", wrongKeySignedToken)});

        LoginMember loginMember = loginMemberResolver.resolve(request);

        assertThat(loginMember).isNull();
    }

    @Test
    void 만료된_토큰이면_null을_반환한다() {
        String expiredToken = jwtUtils.createToken(1L, "어드민", "ADMIN", -1000);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("token", expiredToken)});

        LoginMember loginMember = loginMemberResolver.resolve(request);

        assertThat(loginMember).isNull();
    }

    @Test
    void 쿠키가_없으면_null을_반환한다() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        LoginMember loginMember = loginMemberResolver.resolve(request);

        assertThat(loginMember).isNull();
    }
}
