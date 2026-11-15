package roomescape.member;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.IntegrationTestSupport;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MemberIntegrationTest extends IntegrationTestSupport {

    @Test
    void 로그아웃하면_기존_세션_쿠키로_회원_정보를_조회할_수_없다() {
        // given
        Map<String, String> request = Map.of("email", "admin@email.com", "password", "password");
        String token = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().statusCode(200)
                .extract().cookie("token");

        assertThat(token).isNotBlank();
        RestAssured.given()
                .cookie("token", token)
                .when().get("/login/check")
                .then().statusCode(200);

        // when
        ExtractableResponse<Response> logoutResponse = RestAssured.given()
                .cookie("token", token)
                .when().post("/logout")
                .then().statusCode(200)
                .extract();

        // then
        assertThat(logoutResponse.detailedCookie("token").getMaxAge()).isZero();
        assertThat(logoutResponse.detailedCookie("token").getPath()).isEqualTo("/");

        ExtractableResponse<Response> checkResponse = RestAssured.given()
                .cookie("token", token)
                .when().get("/login/check")
                .then().statusCode(401)
                .contentType(ContentType.JSON)
                .extract();

        assertThat(checkResponse.jsonPath().getMap(""))
                .isEqualTo(Map.of("code", "MEMBER_LOGIN_REQUIRED", "message", "로그인이 필요합니다."));
    }

    @Test
    void 쿠키_없이_회원_정보를_조회하면_401을_반환한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .when().get("/login/check")
                .then().statusCode(401)
                .extract();

        // then
        assertThat(response.jsonPath().getMap(""))
                .isEqualTo(Map.of("code", "MEMBER_LOGIN_REQUIRED", "message", "로그인이 필요합니다."));
        assertThat(response.cookie("token")).isNull();
    }

    @Test
    void 존재하지_않는_세션_쿠키로_회원_정보를_조회하면_401을_반환한다() {
        // given
        String token = "unknown-session-id";

        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .cookie("token", token)
                .when().get("/login/check")
                .then().statusCode(401)
                .extract();

        // then
        assertThat(response.jsonPath().getMap(""))
                .isEqualTo(Map.of("code", "MEMBER_LOGIN_REQUIRED", "message", "로그인이 필요합니다."));
        assertThat(response.cookie("token")).isNull();
    }

    @Test
    void 이메일이_비어있으면_400을_반환한다() {
        // given
        Map<String, String> request = Map.of("email", "", "password", "password");

        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/login")
                .then().statusCode(400)
                .contentType(ContentType.JSON)
                .extract();

        // then
        assertThat(response.jsonPath().getString("code")).isEqualTo("GLOBAL_BAD_REQUEST");
        assertThat(response.jsonPath().getString("message")).isEqualTo("이메일은 비어 있을 수 없습니다.");
        assertThat(response.cookie("token")).isNull();
    }

    @Test
    void 지원하지_않는_HTTP_메서드이면_405를_반환한다() {
        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .when().put("/login")
                .then().statusCode(405)
                .contentType(ContentType.JSON)
                .extract();

        // then
        assertThat(response.jsonPath().getString("code")).isEqualTo("GLOBAL_METHOD_NOT_ALLOWED");
        assertThat(response.jsonPath().getString("message")).isEqualTo("지원하지 않는 HTTP 메서드입니다.");
    }

    @Test
    void 지원하지_않는_ContentType이면_415를_반환한다() {
        // given
        String request = "invalid";

        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .contentType(ContentType.TEXT)
                .body(request)
                .when().post("/login")
                .then().statusCode(415)
                .contentType(ContentType.JSON)
                .extract();

        // then
        assertThat(response.jsonPath().getString("code")).isEqualTo("GLOBAL_UNSUPPORTED_MEDIA_TYPE");
        assertThat(response.jsonPath().getString("message")).isEqualTo("지원하지 않는 Content-Type입니다.");
    }

    @ParameterizedTest
    @CsvSource({"admin@email.com, wrong-password", "unknown@email.com, password"})
    void 이메일이나_비밀번호가_일치하지_않으면_동일한_401_응답을_반환한다(
            String email, String password
    ) {
        // when
        ExtractableResponse<Response> response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when().post("/login")
                .then().statusCode(401)
                .contentType(ContentType.JSON)
                .extract();

        // then
        assertThat(response.jsonPath().getMap(""))
                .isEqualTo(Map.of(
                        "code", "MEMBER_LOGIN_FAILED",
                        "message", "이메일 또는 비밀번호가 올바르지 않습니다."
                ));
        assertThat(response.cookie("token")).isNull();
    }

    @Test
    void 로그인_응답의_세션_쿠키로_회원_이름을_조회한다() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("email", "admin@email.com");
        params.put("password", "password");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract();

        // then
        String token = response.cookie("token");

        assertThat(token).isNotBlank();
        assertThat(response.detailedCookie("token").getPath()).isEqualTo("/");
        assertThat(response.detailedCookie("token").isHttpOnly()).isTrue();

        ExtractableResponse<Response> checkResponse = RestAssured.given()
                .cookie("token", token)
                .when().get("/login/check")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract();

        assertThat(checkResponse.body().jsonPath().getMap(""))
                .isEqualTo(Map.of("name", "어드민"));
    }
}
