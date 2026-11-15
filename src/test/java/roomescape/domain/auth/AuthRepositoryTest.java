package roomescape.domain.auth;

import auth.principal.LoginMember;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.auth.repository.AuthRepository;
import roomescape.global.data.SchemaInitializer;
import roomescape.global.data.SchemaInitializerDependency;
import roomescape.global.data.TestDataLoader;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({SchemaInitializer.class, SchemaInitializerDependency.class, TestDataLoader.class})
public class AuthRepositoryTest {

    @Autowired
    private AuthRepository authRepository;

    @Test
    void findByEmailAndPassword를_호출하면_일치하는_LoginMember를_반환한다() {
        // when
        Optional<LoginMember> foundMember = authRepository.findByEmailAndPassword("admin@dummy.com", "dummy");

        // then
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().name()).isEqualTo("더미_어드민");
        assertThat(foundMember.get().role()).isEqualTo("ADMIN");
    }

    @Test
    void findByEmailAndPassword에_일치하는_회원이_없으면_빈_Optional을_반환한다() {
        // when
        Optional<LoginMember> foundMember = authRepository.findByEmailAndPassword("admin@dummy.com", "wrong-password");

        // then
        assertThat(foundMember).isEmpty();
    }
}
