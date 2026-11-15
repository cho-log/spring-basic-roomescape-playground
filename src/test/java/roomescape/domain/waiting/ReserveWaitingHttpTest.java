package roomescape.domain.waiting;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReserveWaitingHttpTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = this.port;
    }

    @Test
    void 토큰_없이_예약_대기를_생성하면_401을_응답한다() {
        // given
        Map<String, String> params = waitingParams(LocalDate.now().plusDays(1), "1", "1");

        // when & then
        RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 토큰_없이_예약_대기를_삭제하면_401을_응답한다() {
        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().delete("/waitings/1")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void 예약_대기_생성에_성공하면_201과_Location_헤더_직렬화된_바디를_응답한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = waitingParams(LocalDate.now().plusDays(1), "1", "1");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", containsString("/waitings/"))
                .body("id", notNullValue())
                .body("waitingNumber", notNullValue());
    }

    @Test
    void 날짜_필드가_비어_있으면_400을_응답한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = waitingParams(null, "1", "1");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 과거_날짜로_예약_대기를_생성하면_400을_응답한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = waitingParams(LocalDate.now().minusDays(1), "1", "1");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 형식이_잘못된_날짜는_400을_응답한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = new HashMap<>();
        params.put("date", "2026/12/31");
        params.put("time", "1");
        params.put("theme", "1");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 존재하지_않는_시각으로_예약_대기를_생성하면_404를_응답한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = waitingParams(LocalDate.now().plusDays(1), "999", "1");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 존재하지_않는_테마로_예약_대기를_생성하면_404를_응답한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = waitingParams(LocalDate.now().plusDays(1), "1", "999");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 이미_예약한_건에_예약_대기를_생성하면_400을_응답한다() {
        // given
        String token = createToken("user@dummy.com", "dummy");
        Map<String, String> params = waitingParams(LocalDate.of(9999, 12, 31), "1", "1");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 이미_예약_대기중인_건에_다시_예약_대기를_생성하면_409를_응답한다() {
        // given
        String token = createToken("user@dummy.com", "dummy");
        Map<String, String> params = waitingParams(LocalDate.of(9999, 1, 1), "1", "1");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void 자신의_예약_대기_삭제에_성공하면_204를_응답한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = waitingParams(LocalDate.now().plusDays(1), "1", "1");

        Long waitingId = RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/waitings")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract().jsonPath().getLong("id");

        // when & then
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/waitings/" + waitingId)
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void 다른_사람의_예약_대기를_삭제하면_404를_응답한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        // when & then
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/waitings/1")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    private Map<String, String> waitingParams(LocalDate date, String timeId, String themeId) {
        Map<String, String> params = new HashMap<>();
        params.put("date", date == null ? null : date.toString());
        params.put("time", timeId);
        params.put("theme", themeId);
        return params;
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
