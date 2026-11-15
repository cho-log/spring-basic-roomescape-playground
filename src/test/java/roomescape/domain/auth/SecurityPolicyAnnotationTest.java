package roomescape.domain.auth;

import auth.support.annotation.AdminOnly;
import auth.support.annotation.LoginRequired;
import auth.support.annotation.Public;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SecurityPolicyAnnotationTest {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void 모든_컨트롤러_메소드는_공개_혹은_인가_정책_어노테이션을_정확히_하나_선언한다() {
        List<String> violations = new ArrayList<>();

        handlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {
            if (!handlerMethod.getBeanType().getPackageName().startsWith("roomescape")) {
                return;
            }

            int policyCount = 0;
            if (handlerMethod.hasMethodAnnotation(Public.class)) {
                policyCount++;
            }
            if (handlerMethod.hasMethodAnnotation(LoginRequired.class)) {
                policyCount++;
            }
            if (handlerMethod.hasMethodAnnotation(AdminOnly.class)) {
                policyCount++;
            }

            if (policyCount != 1) {
                violations.add("%s %s.%s (정책 어노테이션 %d개)".formatted(
                        mappingInfo,
                        handlerMethod.getBeanType().getSimpleName(),
                        handlerMethod.getMethod().getName(),
                        policyCount
                ));
            }
        });

        assertThat(violations).isEmpty();
    }

    @Test
    void admin_경로의_컨트롤러_메소드는_관리자_전용_어노테이션을_선언한다() {
        List<String> violations = new ArrayList<>();

        handlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {
            if (!handlerMethod.getBeanType().getPackageName().startsWith("roomescape")) {
                return;
            }

            boolean adminPath = mappingInfo.getPathPatternsCondition().getPatternValues().stream()
                    .anyMatch(pattern -> pattern.equals("/admin") || pattern.startsWith("/admin/"));

            if (adminPath && !handlerMethod.hasMethodAnnotation(AdminOnly.class)) {
                violations.add("%s %s.%s".formatted(
                        mappingInfo,
                        handlerMethod.getBeanType().getSimpleName(),
                        handlerMethod.getMethod().getName()
                ));
            }
        });

        assertThat(violations).isEmpty();
    }
}
