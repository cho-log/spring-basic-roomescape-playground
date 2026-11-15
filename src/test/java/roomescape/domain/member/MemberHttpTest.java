package roomescape.domain.member;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MemberHttpTest {

    private final String name = "Alice";
    private final String email = "alice@test.com";
    private final String password = "test";

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = this.port;
    }

    @Test
    void 회원가입에_성공한다() {
        // given
        Map<String, String> params = signupParams(name, email, password);

        // when & then
        RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", containsString("/members"))
                .body("id", notNullValue())
                .body("name", is(name))
                .body("email", is(email));
    }

    @Test
    void 회원가입시_name이_비어_있으면_400을_반환한다() {

        // name == null
        postSignupExpectingBadRequest(signupParams(null, email, password));

        // name == ""
        postSignupExpectingBadRequest(signupParams("", email, password));

        // name == " "
        postSignupExpectingBadRequest(signupParams(" ", email, password));
    }

    @Test
    void 회원가입시_email이_비어_있으면_400을_반환한다() {

        // email == null
        postSignupExpectingBadRequest(signupParams(name, null, password));

        // email == ""
        postSignupExpectingBadRequest(signupParams(name, "", password));

        // email == " "
        postSignupExpectingBadRequest(signupParams(name, " ", password));
    }

    @Test
    void 회원가입시_email_형식이_올바르지_않으면_400을_반환한다() {

        // email not contains '@'
        postSignupExpectingBadRequest(signupParams(name, "alicetest.com", password));
    }

    @Test
    void 회원가입시_password가_비어_있으면_400을_반환한다() {

        // password == null
        postSignupExpectingBadRequest(signupParams(name, email, null));

        // password == ""
        postSignupExpectingBadRequest(signupParams(name, email, ""));

        // password == " "
        postSignupExpectingBadRequest(signupParams(name, email, " "));
    }

    @Test
    void 로그인에_성공하면_token_쿠키를_발급한다() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("email", "admin@dummy.com");
        params.put("password", "dummy");

        // when
        String token = RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("token");

        // then
        assertThat(token).isNotBlank();
    }

    @Test
    void 로그인시_email이나_password가_비어_있으면_400을_반환한다() {

        // email == null
        Map<String, String> emptyEmailParams = new HashMap<>();
        emptyEmailParams.put("email", null);
        emptyEmailParams.put("password", "password");

        RestAssured.given()
                .body(emptyEmailParams)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // password == null
        Map<String, String> emptyPasswordParams = new HashMap<>();
        emptyPasswordParams.put("email", "admin@email.com");
        emptyPasswordParams.put("password", null);

        RestAssured.given()
                .body(emptyPasswordParams)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 회원가입한_계정으로_로그인할_수_있다() {
        // given
        RestAssured.given()
                .body(signupParams(name, email, password))
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("email", email);
        loginParams.put("password", password);

        // when
        String token = RestAssured.given()
                .body(loginParams)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("token");

        // then
        assertThat(token).isNotBlank();
    }

    @Test
    void 로그인한_사용자의_이름을_조회한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        // when & then
        RestAssured.given()
                .cookie("token",token)
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("name", is("더미_어드민"));
    }

    @Test
    void 로그아웃하면_token_쿠키를_만료시킨다() {
        // when
        Cookie tokenCookie = RestAssured.given()
                .contentType(ContentType.JSON)
                .when().post("/logout")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().detailedCookie("token");

        // then
        assertThat(tokenCookie.getValue()).isEmpty();
        assertThat(tokenCookie.getMaxAge()).isEqualTo(0);
    }

    @Test
    void 중복된_email로_회원가입하면_409를_반환한다() {
        // given
        RestAssured.given()
                .body(signupParams(name, email, password))
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // when & then
        RestAssured.given()
                .body(signupParams("Bob", email, password))
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void 중복된_name으로_회원가입하면_409를_반환한다() {
        // given
        RestAssured.given()
                .body(signupParams(name, email, password))
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // when & then
        RestAssured.given()
                .body(signupParams(name, "bob@test.com", password))
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void 잘못된_비밀번호로_로그인하면_401을_반환한다() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("email", "admin@dummy.com");
        params.put("password", "wrong-password");

        // when & then
        RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 로그인하지_않고_사용자_이름을_조회하면_401을_반환한다() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 로그아웃하면_기존_세션으로_사용자_이름을_조회할_수_없다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/logout")
                .then()
                .statusCode(HttpStatus.OK.value());

        // when & then
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().get("/login/check")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    private Map<String, String> signupParams(String name, String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("password", password);
        return params;
    }

    private void postSignupExpectingBadRequest(Map<String, String> params) {
        RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private String createToken(String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        return RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("token");
    }
}
