package roomescape.auth;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class AuthenticationExceptionTest {

    @Test
    void 쿠키가_없으면_401을_응답한다() {
        RestAssured.given().log().all()
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 토큰이_빈값이면_401을_응답한다() {
        RestAssured.given().log().all()
                .cookie("token", "")
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 관련없는_쿠키만_있으면_401을_응답한다() {
        RestAssured.given().log().all()
                .cookie("other", "value")
                .when().get("/login/check")
                .then().log().all()
                .statusCode(401);
    }
}