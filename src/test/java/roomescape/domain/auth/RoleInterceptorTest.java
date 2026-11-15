package roomescape.domain.auth;

import auth.exception.AuthException;
import auth.support.RoleInterceptor;
import auth.support.SessionManager;
import auth.support.annotation.AdminOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleInterceptorTest {

    private final SessionManager sessionManager = new SessionManager("token");

    @Test
    void 루트_패키지_안의_핸들러는_인가_정책을_검사한다() throws NoSuchMethodException {
        // given
        // 핸들러 패키지: roomescape.domain.auth
        HandlerMethod handlerMethod = new HandlerMethod(new DummyController(), "adminOnly");

        // rootPackage == 핸들러와 같은 패키지
        assertUnauthorized(new RoleInterceptor(sessionManager, "roomescape.domain.auth"), handlerMethod);

        // rootPackage == 핸들러의 상위 패키지
        assertUnauthorized(new RoleInterceptor(sessionManager, "roomescape"), handlerMethod);
    }

    @Test
    void 루트_패키지_밖의_핸들러는_로그인_없이도_통과한다() throws Exception {
        // given
        // 핸들러 패키지(roomescape.domain.auth)는 루트 패키지(auth) 밖에 있다.
        RoleInterceptor roleInterceptor = new RoleInterceptor(sessionManager, "auth");
        HandlerMethod handlerMethod = new HandlerMethod(new DummyController(), "adminOnly");

        // when
        boolean result = roleInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handlerMethod);

        // then
        assertThat(result).isTrue();
    }

    private void assertUnauthorized(RoleInterceptor roleInterceptor, HandlerMethod handlerMethod) {
        AuthException exception = Assertions.assertThrows(
                AuthException.class,
                () -> roleInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handlerMethod)
        );

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    static class DummyController {

        @AdminOnly
        public void adminOnly() {
        }
    }
}
