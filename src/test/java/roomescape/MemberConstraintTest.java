package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class MemberConstraintTest {

    @Test
    void 빈_요청으로_가입하면_400() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new HashMap<>())
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }

    @ParameterizedTest
    @CsvSource({
            "'', test@email.com, password",
            "테스트, '', password",
            "테스트, test@email.com, ''"
    })
    void 빈_문자열이_섞여_있으면_400(String name, String email, String password) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParams(name, email, password))
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 빈_요청으로_로그인하면_401() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new HashMap<>())
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 같은_이메일로_두_번_가입하면_409() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParams("테스트", "test@email.com", "password"))
                .when().post("/members")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParams("테스트", "test@email.com", "password"))
                .when().post("/members")
                .then().log().all()
                .statusCode(409);
    }

    private Map<String, String> memberParams(String name, String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("password", password);
        return params;
    }
}
