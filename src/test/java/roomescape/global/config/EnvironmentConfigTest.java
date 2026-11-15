package roomescape.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EnvironmentConfigTest {

    @Value("${server.servlet.session.cookie.name}")
    private String sessionCookieName;

    @Test
    void 세션_쿠키_이름을_외부_설정에서_주입받는다() {
        assertThat(sessionCookieName).isNotBlank();
    }
}
