package roomescape.domain.auth;

import auth.principal.LoginMember;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginMemberTest {

    private final Long id = 1L;
    private final String name = "Alice";
    private final String email = "test@test.com";
    private final String userRole = "USER";
    private final String adminRole = "ADMIN";

    @Test
    void LoginMember는_id가_빈_채로_생성할_수_없다() {

        // id == null
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(null, name, email, userRole)
        );
    }

    @Test
    void LoginMember는_name이_빈_채로_생성할_수_없다() {

        // name == null
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, null, email, userRole)
        );

        // name == ""
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, "", email, userRole)
        );

        // name == " "
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, " ", email, userRole)
        );
    }

    @Test
    void LoginMember는_email이_빈_채로_생성할_수_없다() {

        // email == null
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, null, userRole)
        );

        // email == ""
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, "", userRole)
        );

        // email == " "
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, " ", userRole)
        );
    }

    @Test
    void LoginMember의_email은_이메일_정규식_이외의_값을_허용하지_않는다() {

        // email not contains '@'
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, "testtest.com", userRole)
        );

        // email not ends with '.com'
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, "test@test.org", userRole)
        );
    }

    @Test
    void LoginMember는_role이_빈_채로_생성할_수_없다() {

        // role == null
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, email, null)
        );

        // role == ""
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, email, "")
        );

        // role == " "
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, email, " ")
        );
    }

    @Test
    void LoginMember는_role은_USER와_ADMIN_이외의_값을_지정할_수_없다() {

        // role != "USER" && role != "ADMIN"
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LoginMember(id, name, email, "ASDF")
        );
    }

    @Test
    void role이_USER인_LoginMember를_정상적으로_생성한_경우() {
        // given
        LoginMember loginMember = new LoginMember(id, name, email, userRole);

        // then
        assertThat(loginMember.id()).isEqualTo(id);
        assertThat(loginMember.name()).isEqualTo(name);
        assertThat(loginMember.email()).isEqualTo(email);
        assertThat(loginMember.role()).isEqualTo(userRole);
        assertThat(loginMember.isAdmin()).isFalse();
    }

    @Test
    void role이_ADMIN인_LoginMember를_정상적으로_생성한_경우() {
        // given
        LoginMember loginMember = new LoginMember(id, name, email, adminRole);

        // then
        assertThat(loginMember.id()).isEqualTo(id);
        assertThat(loginMember.name()).isEqualTo(name);
        assertThat(loginMember.email()).isEqualTo(email);
        assertThat(loginMember.role()).isEqualTo(adminRole);
        assertThat(loginMember.isAdmin()).isTrue();
    }
}
