package roomescape.domain.time;

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
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.domain.waiting.repository.ReserveWaitingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TimeHttpTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TimeRepository timeRepository;

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
    void 시간_생성에_성공한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = new HashMap<>();
        params.put("value", "10:00");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/times")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .header("Location", containsString("/times"))
                .body("id", notNullValue())
                .body("value", is("10:00"));
    }

    @Test
    void 시간은_value가_빈_채로_생성할_수_없다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Map<String, String> params = new HashMap<>();
        params.put("value", null);

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/times")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void 이미_존재하는_value로_시간을_생성하면_409를_반환한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        timeRepository.save(new Time(LocalTime.of(10, 0)));

        Map<String, String> params = new HashMap<>();
        params.put("value", "10:00");

        // when & then
        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/times")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void 시간_목록_조회에_성공한다() {
        // given
        timeRepository.save(new Time(LocalTime.of(10, 0)));
        timeRepository.save(new Time(LocalTime.of(12, 0)));

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(3))
                .body("value", hasItems("00:00", "10:00", "12:00"));
    }

    @Test
    void 시간_삭제에_성공한다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Time savedTime = timeRepository.save(new Time(LocalTime.of(10, 0)));

        // when
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/times/" + savedTime.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }

    @Test
    void 일반_유저는_시간을_삭제할_수_없다() {
        // given
        String token = createToken("user@dummy.com", "dummy");
        Time savedTime = timeRepository.save(new Time(LocalTime.of(10, 0)));

        // when & then
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/times/" + savedTime.getId())
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void 예약이_있는_시간은_삭제할_수_없다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        // data-test.sql: 1번 시간을 예약만 참조하도록 예약 대기를 삭제
        reserveWaitingRepository.deleteById(1L);

        // when
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/times/1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }

    @Test
    void 예약_대기가_있는_시간은_삭제할_수_없다() {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        // data-test.sql: 1번 시간을 예약 대기만 참조하도록 예약을 삭제
        reservationRepository.deleteById(1L);

        // when
        RestAssured.given()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().delete("/times/1")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());

        // then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/times")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1));
    }

    @Test
    void 예약_가능_시간_조회에_성공한다() {
        // given
        timeRepository.save(new Time(LocalTime.of(10, 0)));
        timeRepository.save(new Time(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("Dummy", "it is Dummy for test."));

        String date = LocalDate.now().plusDays(1).toString();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/available-times?date=" + date + "&themeId=" + theme.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(3))
                .body("time", hasItems("00:00", "10:00", "12:00"));
    }

    @Test
    void 예약_가능_시간은_과거_날짜로_조회할_수_없다() {
        // given
        Theme theme = themeRepository.save(new Theme("Dummy", "it is Dummy for test."));

        // date == 어제
        getAvailableTimesExpectingBadRequest(LocalDate.now().minusDays(1).toString(), theme.getId());

        // date == 먼 과거
        getAvailableTimesExpectingBadRequest(LocalDate.of(1970, 1, 1).toString(), theme.getId());
    }

    @Test
    void 오늘_날짜로_예약_가능_시간_조회에_성공한다() {
        // given
        Theme theme = themeRepository.save(new Theme("Dummy", "it is Dummy for test."));
        String date = LocalDate.now().toString();

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/available-times?date=" + date + "&themeId=" + theme.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(1))
                .body("time", hasItems("00:00"))
                .body("booked", hasItems(false));
    }

    private void getAvailableTimesExpectingBadRequest(String date, Long themeId) {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .when().get("/available-times?date=" + date + "&themeId=" + themeId)
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
