package roomescape.member;

import auth.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import roomescape.exception.UnauthorizedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LoginMemberArgumentResolverTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private NativeWebRequest webRequest;

    private LoginMemberArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new LoginMemberArgumentResolver(jwtUtils);
    }

    @Test
    void LoginMember_타입의_파라미터인_경우_true를_반환한다() {
        // given
        MethodParameter parameter = mock(MethodParameter.class);
        given(parameter.getParameterType()).willReturn((Class) LoginMember.class);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void LoginMember_타입이_아닌_경우_false를_반환한다() {
        // given
        MethodParameter parameter = mock(MethodParameter.class);
        given(parameter.getParameterType()).willReturn((Class) String.class);

        // when
        boolean result = resolver.supportsParameter(parameter);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 유효한_토큰_쿠키가_있으면_LoginMember_객체를_반환한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("token", "valid-token"));
        given(webRequest.getNativeRequest()).willReturn(request);

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("name", String.class)).willReturn("어드민");
        given(claims.get("email", String.class)).willReturn("admin@email.com");
        given(claims.get("role", String.class)).willReturn("ADMIN");

        given(jwtUtils.getClaims("valid-token")).willReturn(claims);

        // when
        Object result = resolver.resolveArgument(null, null, webRequest, null);

        // then
        assertThat(result).isInstanceOf(LoginMember.class);
        LoginMember loginMember = (LoginMember) result;
        assertThat(loginMember.getId()).isEqualTo(1L);
        assertThat(loginMember.getName()).isEqualTo("어드민");
        assertThat(loginMember.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void 쿠키가_없거나_토큰이_없으면_UnauthorizedException이_발생한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        given(webRequest.getNativeRequest()).willReturn(request);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("로그인이 필요합니다.");
    }
}
