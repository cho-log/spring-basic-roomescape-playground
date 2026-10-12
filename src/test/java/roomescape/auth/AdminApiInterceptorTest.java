package roomescape.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class AdminApiInterceptorTest {

    private String createToken(String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract();

        return response.headers().get("Set-Cookie").getValue().split(";")[0].split("=")[1];
    }

    @Test
    void 쿠키_없이_시간_생성을_요청하면_401을_응답한다() {
        Map<String, String> params = new HashMap<>();
        params.put("value", "23:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 어드민이_아니면_시간_생성을_요청하면_401을_응답한다() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("value", "23:00");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 어드민이면_시간_생성이_가능하다() {
        String adminToken = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("value", "23:00");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 로그인_없이도_시간_목록_조회는_가능하다() {
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(200);
    }
}
