package roomescape.domain.auth;

import auth.principal.LoginMember;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.auth.service.AuthService;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;
import roomescape.global.exception.UnauthorizedException;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AuthService.class, SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class AuthServiceTest {

    private String name = "더미_유저";
    private String email = "user@dummy.com";
    private String password = "dummy";

    @Autowired
    private AuthService authService;

    @Test
    void 올바른_email과_password로_login_함수를_호출하면_LoginMember를_반환한다() {
        // when
        LoginMember loginMember = authService.login(email, password);

        // then
        assertThat(loginMember).isNotNull();
        assertThat(loginMember.id()).isNotNull();
        assertThat(loginMember.name()).isEqualTo(name);
        assertThat(loginMember.email()).isEqualTo(email);
    }

    @Test
    void 잘못된_email로_login_함수를_호출하면_401_예외를_던진다() {
        // then
        Assertions.assertThrows(
                UnauthorizedException.class,

                // when
                () ->  authService.login("wrong" + email, password)
        );
    }

    @Test
    void 잘못된_password로_login_함수를_호출하면_401_예외를_던진다() {
        // then
        Assertions.assertThrows(
                UnauthorizedException.class,

                // when
                () ->  authService.login(email, "wrong" + password)
        );
    }
}
