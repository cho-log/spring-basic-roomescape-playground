package roomescape;

import auth.JwtUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.MyReservationResponse;
import roomescape.reservation.ReservationResponse;
import roomescape.waiting.WaitingResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class MissionStepTest {

    private static final String FUTURE_DATE = "2030-03-01";
    private static final String LEAP_DATE = "2032-02-29";
    private static final String NOT_LEAP_DATE = "2030-02-29";
    private static final String PAST_DATE = "2024-03-01";

    @Test
    void 일단계() {
        String token = createToken("admin@email.com", "password");

        assertThat(token).isNotBlank();

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .when().get("/login/check")
                .then().log().all()
                .statusCode(200)
                .extract();

        assertThat(response.body().jsonPath().getString("name")).isEqualTo("어드민");
    }

    @Test
    void 이단계() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", FUTURE_DATE);
        params.put("time", "1");
        params.put("theme", "1");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.as(ReservationResponse.class).getName()).isEqualTo("어드민");

        params.put("name", "브라운");

        ExtractableResponse<Response> adminResponse = RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then().log().all()
                .extract();

        assertThat(adminResponse.statusCode()).isEqualTo(201);
        assertThat(adminResponse.as(ReservationResponse.class).getName()).isEqualTo("브라운");
    }

    @Test
    void 삼단계() {
        String brownToken = createToken("brown@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .get("/admin")
                .then().log().all()
                .statusCode(401);

        String adminToken = createToken("admin@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    void 오단계() {
        String adminToken = createToken("admin@email.com", "password");

        List<MyReservationResponse> reservations = RestAssured.given().log().all()
                .cookie("token", adminToken)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        assertThat(reservations).hasSize(3);
    }

    @Test
    void 육단계() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", FUTURE_DATE);
        params.put("time", "1");
        params.put("theme", "1");

        // 예약 대기 생성
        WaitingResponse waiting = RestAssured.given().log().all()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        // 내 예약 목록 조회
        List<MyReservationResponse> myReservations = RestAssured.given().log().all()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        // 예약 대기 상태 확인
        String status = myReservations.stream()
                .filter(it -> it.reservationId().equals(waiting.id()))
                .filter(it -> !it.status().equals("예약"))
                .findFirst()
                .map(MyReservationResponse::status)
                .orElse(null);

        assertThat(status).isEqualTo("1번째 예약대기");

        // 예약 대기 취소
        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .delete("/waitings/" + waiting.id())
                .then().log().all()
                .statusCode(204);

        // 취소 후 내 예약 목록에서 대기가 사라졌는지 확인
        List<MyReservationResponse> afterCancel = RestAssured.given().log().all()
                .cookie("token", brownToken)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        assertThat(afterCancel).noneMatch(it -> !it.status().equals("예약"));
    }

    @Test
    void 칠단계() {
        Component componentAnnotation = JwtUtils.class.getAnnotation(Component.class);
        assertThat(componentAnnotation).isNull();
    }

    @Nested
    class 인증_실패 {

        @ParameterizedTest
        @CsvSource({
                "wrong@email.com, password",
                "admin@email.com, wrong-password"
        })
        void 이메일이나_비밀번호가_틀리면_401(String email, String password) {
            Map<String, String> params = new HashMap<>();
            params.put("email", email);
            params.put("password", password);

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/login")
                    .then().log().all()
                    .statusCode(401);
        }

        @Test
        void 토큰_없이_인증_정보를_조회하면_401() {
            RestAssured.given().log().all()
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(401);
        }

        @Test
        void 잘못된_토큰으로_인증_정보를_조회하면_401() {
            RestAssured.given().log().all()
                    .cookie("token", "invalid-token")
                    .when().get("/login/check")
                    .then().log().all()
                    .statusCode(401);
        }
    }

    @Nested
    class 예약_대기_실패 {

        @Test
        void 이미_예약한_시간에_대기하면_409() {
            String adminToken = createToken("admin@email.com", "password");

            requestReservation(adminToken, FUTURE_DATE, "1", "1")
                    .statusCode(201);

            requestWaiting(adminToken, FUTURE_DATE, "1", "1")
                    .statusCode(409);
        }

        @Test
        void 이미_대기_중인_시간에_다시_대기하면_409() {
            String brownToken = createToken("brown@email.com", "password");

            requestWaiting(brownToken, FUTURE_DATE, "1", "1")
                    .statusCode(201);

            requestWaiting(brownToken, FUTURE_DATE, "1", "1")
                    .statusCode(409);
        }

        @Test
        void 다른_사람의_대기를_취소하면_401() {
            String brownToken = createToken("brown@email.com", "password");

            WaitingResponse waiting = requestWaiting(brownToken, FUTURE_DATE, "1", "1")
                    .statusCode(201)
                    .extract().as(WaitingResponse.class);

            String adminToken = createToken("admin@email.com", "password");

            RestAssured.given().log().all()
                    .cookie("token", adminToken)
                    .delete("/waitings/" + waiting.id())
                    .then().log().all()
                    .statusCode(401);
        }
    }

    @Nested
    class 예약_대기_순번 {

        private static final String DATE = FUTURE_DATE;
        private static final String TIME = "1";
        private static final String THEME = "1";

        @Test
        void 같은_슬롯에_대기하면_순번이_차례로_매겨진다() {
            String brownToken = createToken("brown@email.com", "password");
            String testToken = createMemberToken("테스트", "test@email.com", "password");

            WaitingResponse first = requestWaiting(brownToken, DATE, TIME, THEME)
                    .statusCode(201)
                    .extract().as(WaitingResponse.class);
            WaitingResponse second = requestWaiting(testToken, DATE, TIME, THEME)
                    .statusCode(201)
                    .extract().as(WaitingResponse.class);

            assertThat(first.waitingNumber()).isEqualTo(1);
            assertThat(second.waitingNumber()).isEqualTo(2);
        }

        @Test
        void 앞선_대기가_취소되면_순번이_당겨진다() {
            String brownToken = createToken("brown@email.com", "password");
            String testToken = createMemberToken("테스트", "test@email.com", "password");

            WaitingResponse brownWaiting = requestWaiting(brownToken, DATE, TIME, THEME)
                    .statusCode(201)
                    .extract().as(WaitingResponse.class);
            requestWaiting(testToken, DATE, TIME, THEME)
                    .statusCode(201);

            assertThat(waitingStatusOf(testToken)).isEqualTo("2번째 예약대기");

            RestAssured.given().log().all()
                    .cookie("token", brownToken)
                    .delete("/waitings/" + brownWaiting.id())
                    .then().log().all()
                    .statusCode(204);

            assertThat(waitingStatusOf(testToken)).isEqualTo("1번째 예약대기");
        }
    }

    @Nested
    class 삭제된_시간_테마 {

        @Test
        void 삭제된_시간으로_대기하면_400() {
            String brownToken = createToken("brown@email.com", "password");
            deleteTime("3");

            requestWaiting(brownToken, FUTURE_DATE, "3", "1")
                    .statusCode(400);
        }

        @Test
        void 삭제된_테마로_대기하면_400() {
            String brownToken = createToken("brown@email.com", "password");
            deleteTheme("2");

            requestWaiting(brownToken, FUTURE_DATE, "1", "2")
                    .statusCode(400);
        }

        @Test
        void 삭제된_시간으로_예약하면_400() {
            String brownToken = createToken("brown@email.com", "password");
            deleteTime("3");

            requestReservation(brownToken, FUTURE_DATE, "3", "1")
                    .statusCode(400);
        }

        @Test
        void 삭제된_테마로_예약하면_400() {
            String brownToken = createToken("brown@email.com", "password");
            deleteTheme("2");

            requestReservation(brownToken, FUTURE_DATE, "1", "2")
                    .statusCode(400);
        }
    }

    @Nested
    class 잘못된_날짜 {

        @ParameterizedTest
        @CsvSource({
                "''",
                "not-a-date",
                "2024-02-31",
                "2024-3-5"
        })
        void 달력에_없는_날짜로_예약하거나_대기하면_400(String date) {
            String brownToken = createToken("brown@email.com", "password");

            requestReservation(brownToken, date, "1", "1")
                    .statusCode(400);

            requestWaiting(brownToken, date, "1", "1")
                    .statusCode(400);
        }

        @Test
        void 윤년의_2월_29일은_예약할_수_있다() {
            String brownToken = createToken("brown@email.com", "password");

            requestReservation(brownToken, LEAP_DATE, "1", "1")
                    .statusCode(201);
        }

        @Test
        void 윤년이_아닌_해의_2월_29일로_예약하면_400() {
            String brownToken = createToken("brown@email.com", "password");

            requestReservation(brownToken, NOT_LEAP_DATE, "1", "1")
                    .statusCode(400);
        }
    }

    @Nested
    class 지난_날짜 {

        @Test
        void 지난_날짜로_예약하거나_대기하면_400() {
            String brownToken = createToken("brown@email.com", "password");

            requestReservation(brownToken, PAST_DATE, "1", "1")
                    .statusCode(400);

            requestWaiting(brownToken, PAST_DATE, "1", "1")
                    .statusCode(400);
        }
    }

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

    private String createMemberToken(String name, String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("password", password);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/members")
                .then().log().all()
                .statusCode(201);

        return createToken(email, password);
    }

    private ValidatableResponse requestWaiting(String token, String date, String time, String theme) {
        Map<String, String> params = new HashMap<>();
        params.put("date", date);
        params.put("time", time);
        params.put("theme", theme);

        return RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then().log().all();
    }

    private ValidatableResponse requestReservation(String token, String date, String time, String theme) {
        Map<String, String> params = new HashMap<>();
        params.put("date", date);
        params.put("time", time);
        params.put("theme", theme);

        return RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then().log().all();
    }

    private void deleteTime(String id) {
        RestAssured.given().log().all()
                .delete("/times/" + id)
                .then().log().all()
                .statusCode(204);
    }

    private void deleteTheme(String id) {
        RestAssured.given().log().all()
                .delete("/themes/" + id)
                .then().log().all()
                .statusCode(204);
    }

    private String waitingStatusOf(String token) {
        List<MyReservationResponse> myReservations = RestAssured.given().log().all()
                .cookie("token", token)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        return myReservations.stream()
                .filter(it -> !it.status().equals("예약"))
                .findFirst()
                .map(MyReservationResponse::status)
                .orElse(null);
    }
}
