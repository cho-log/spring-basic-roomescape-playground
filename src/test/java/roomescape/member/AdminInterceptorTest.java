package roomescape.member;

import auth.JwtUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import roomescape.exception.UnauthorizedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminInterceptorTest {
    private final JwtUtils jwtUtils = new JwtUtils("Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=");
    private AdminInterceptor adminInterceptor;

    @BeforeEach
    void setUp() {
        adminInterceptor = new AdminInterceptor(jwtUtils);
    }

    @Test
    void 쿠키가_없으면_예외가_발생한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when & then
        assertThatThrownBy(() -> adminInterceptor.preHandle(request, response, null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 일반_유저_권한이면_403_상태코드를_반환하고_요청을_차단한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = jwtUtils.createToken(1L, "일반유저", "user@email.com", "USER");
        request.setCookies(new Cookie("token", token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = adminInterceptor.preHandle(request, response, null);

        // then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void 관리자_권한이면_요청을_통과시킨다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = jwtUtils.createToken(2L, "관리자", "admin@email.com", "ADMIN");
        request.setCookies(new Cookie("token", token));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = adminInterceptor.preHandle(request, response, null);

        // then
        assertThat(result).isTrue();
    }
}
