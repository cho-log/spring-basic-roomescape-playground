package roomescape.domain.theme;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.waiting.repository.ReserveWaitingRepository;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeHttpTest {

    private final String name = "Dummy";
    private final String description = "it is Dummy for Test";

    @LocalServerPort
    private int port;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReserveWaitingRepository reserveWaitingRepository;

    @BeforeEach
    void setup() {
        RestAssured.port = this.port;
    }

    @Test
    void 테마_생성에_성공한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = themeParams(name, description);

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/themes")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", containsString("/themes"))
                .body("id", notNullValue())
                .body("name", is(name))
                .body("description", is(description));
    }

    @Test
    void 일반_유저는_테마를_생성할_수_없다() {
        // given
        String token = createToken("user@dummy.com", "dummy");
        Map<String, String> params = themeParams(name, description);

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/themes")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 테마는_name이_빈_채로_생성할_수_없다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        // name == null
        postThemeExpectingBadRequest(token, themeParams(null, description));

        // name == ""
        postThemeExpectingBadRequest(token, themeParams("", description));

        // name == " "
        postThemeExpectingBadRequest(token, themeParams(" ", description));
    }

    @Test
    void 테마는_description이_빈_채로_생성할_수_없다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        // description == null
        postThemeExpectingBadRequest(token, themeParams(name, null));

        // description == ""
        postThemeExpectingBadRequest(token, themeParams(name, ""));

        // description == " "
        postThemeExpectingBadRequest(token, themeParams(name, " "));
    }

    @Test
    void 이미_존재하는_이름으로_테마를_생성하면_409를_반환한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        themeRepository.save(new Theme(name, description));

        // when & then
        RestAssured.given()
                .body(themeParams(name, description))
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/themes")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void 테마_목록_조회에_성공한다() {
        // given
        themeRepository.save(new Theme("테마A", "테마A입니다."));
        themeRepository.save(new Theme("테마B", "테마B입니다."));

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(3))
                .body("name", hasItems("테마A", "테마B"));
    }

    @Test
    void 테마_삭제에_성공한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Theme savedTheme = themeRepository.save(new Theme(name, description));

        // when
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/themes/" + savedTheme.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }

    @Test
    void 예약이_있는_테마는_삭제할_수_없다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        // data-test.sql: 1번 테마를 예약만 참조하도록 예약 대기를 삭제
        reserveWaitingRepository.deleteById(1L);

        // when
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/themes/1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }

    @Test
    void 예약_대기가_있는_테마는_삭제할_수_없다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        // data-test.sql: 1번 테마를 예약 대기만 참조하도록 예약을 삭제
        reservationRepository.deleteById(1L);

        // when
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/themes/1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/themes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }

    private Map<String, String> themeParams(String name, String description) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        return params;
    }

    private void postThemeExpectingBadRequest(String token, Map<String, String> params) {
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/themes")
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
