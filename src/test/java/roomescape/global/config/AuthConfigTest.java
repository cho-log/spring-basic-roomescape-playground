package roomescape.global.config;

import auth.MemberSessionManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void MemberSessionManager는_Component가_아니다() {
        Component componentAnnotation = MemberSessionManager.class.getAnnotation(Component.class);

        assertThat(componentAnnotation).isNull();
    }

    @Test
    void MemberSessionManager는_설정_클래스를_통해_빈으로_등록된다() {
        MemberSessionManager memberSessionManager = applicationContext.getBean(MemberSessionManager.class);

        assertThat(memberSessionManager).isNotNull();
    }
}
