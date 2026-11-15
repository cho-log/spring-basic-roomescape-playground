package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationType;
import roomescape.waiting.dto.WaitingResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TokenTestSupport.createToken;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest2 {

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
        String adminToken = createToken("admin@email.com", "password");
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "1");

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then().log().all()
                .statusCode(201);

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
        MyReservationResponse waitingReservation = myReservations.stream()
                .filter(it -> it.id().equals(waiting.getId()))
                .filter(it -> it.type() == ReservationType.WAITING)
                .findFirst()
                .orElseThrow();

        assertThat(waitingReservation.waitingRank()).isEqualTo(1L);
    }

    @Test
    void 예약_대기를_취소하면_목록에서_제외된다() {
        String adminToken = createToken("admin@email.com", "password");
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "1");

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then().log().all()
                .statusCode(201);

        WaitingResponse waiting = RestAssured.given().log().all()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .delete("/waitings/" + waiting.getId())
                .then().log().all()
                .statusCode(204);

        List<MyReservationResponse> reservations = RestAssured.given().log().all()
                .cookie("token", brownToken)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        assertThat(reservations).noneMatch(it -> it.type() == ReservationType.WAITING && it.id().equals(waiting.getId()));
    }

    @Test
    void 앞선_대기가_취소되면_남은_대기_순번이_당겨진다() {
        Map<String, String> memberParams = new HashMap<>();
        memberParams.put("name", "한성");
        memberParams.put("email", "hansung@email.com");
        memberParams.put("password", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParams)
                .post("/members")
                .then().log().all()
                .statusCode(201);

        String adminToken = createToken("admin@email.com", "password");
        String brownToken = createToken("brown@email.com", "password");
        String hansungToken = createToken("hansung@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "1");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(params)
                .post("/reservations")
                .then().log().all()
                .statusCode(201);

        WaitingResponse brownWaiting = RestAssured.given().log().all()
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .body(params)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        WaitingResponse hansungWaiting = RestAssured.given().log().all()
                .cookie("token", hansungToken)
                .contentType(ContentType.JSON)
                .body(params)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        assertThat(hansungWaiting.getWaitingNumber()).isEqualTo(2L);

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .delete("/waitings/" + brownWaiting.getId())
                .then().log().all()
                .statusCode(204);

        List<MyReservationResponse> hansungReservations = RestAssured.given().log().all()
                .cookie("token", hansungToken)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        MyReservationResponse hansungWaitingAfterCancel = hansungReservations.stream()
                .filter(it -> it.type() == ReservationType.WAITING)
                .filter(it -> it.id().equals(hansungWaiting.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(hansungWaitingAfterCancel.waitingRank()).isEqualTo(1L);
    }

    @Test
    void 예약을_취소하면_첫_대기가_예약으로_전환되고_다음_대기_순번이_당겨진다() {
        String adminToken = createToken("admin@email.com", "password");
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> memberParams = new HashMap<>();
        memberParams.put("name", "한성");
        memberParams.put("email", "hansung@email.com");
        memberParams.put("password", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(memberParams)
                .post("/members")
                .then().log().all()
                .statusCode(201);

        String hansungToken = createToken("hansung@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "1");

        ReservationResponse reservation = RestAssured.given().log().all()
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .body(params)
                .post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract().as(ReservationResponse.class);

        WaitingResponse waiting = RestAssured.given().log().all()
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .body(params)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        WaitingResponse hansungWaiting = RestAssured.given().log().all()
                .cookie("token", hansungToken)
                .contentType(ContentType.JSON)
                .body(params)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        assertThat(hansungWaiting.getWaitingNumber()).isEqualTo(2L);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .delete("/reservations/" + reservation.getId())
                .then().log().all()
                .statusCode(204);

        List<MyReservationResponse> brownReservations = RestAssured.given().log().all()
                .cookie("token", brownToken)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        assertThat(brownReservations).anyMatch(it -> it.type() == ReservationType.RESERVATION
                        && it.date().equals(reservation.getDate())
                        && it.time().equals(reservation.getTime())
                        && it.theme().equals(reservation.getTheme()));
        assertThat(brownReservations).noneMatch(it -> it.type() == ReservationType.WAITING && it.id().equals(waiting.getId()));

        List<MyReservationResponse> hansungReservations = RestAssured.given().log().all()
                .cookie("token", hansungToken)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        assertThat(hansungReservations).anyMatch(it -> it.type() == ReservationType.WAITING
                        && it.id().equals(hansungWaiting.getId())
                        && it.waitingRank().equals(1L));
    }

    @Test
    @Sql(statements = """
        INSERT INTO waiting (member_id, theme_id, date, time_id)
        VALUES (2, 1, '2024-03-01', 1);
        """)
    void 지난_예약을_삭제하면_대기도_함께_삭제된다() {
        String adminToken = createToken("admin@email.com", "password");
        String brownToken = createToken("brown@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        List<MyReservationResponse> reservations = RestAssured.given().log().all()
                .cookie("token", brownToken)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        assertThat(reservations).noneMatch(it -> it.type() == ReservationType.WAITING
                        && it.date().equals("2024-03-01")
                        && it.time().equals("10:00")
                        && it.theme().equals("테마1"));
    }

    @Test
    void 일반_회원이_본인_예약을_삭제할_수_있다() {
        String brownToken = createToken("brown@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .when().delete("/reservations/4")
                .then().log().all()
                .statusCode(204);

        List<MyReservationResponse> reservations = RestAssured.given().log().all()
                .cookie("token", brownToken)
                .when().get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        assertThat(reservations).noneMatch(it ->
                it.type() == ReservationType.RESERVATION && it.id().equals(4L));
    }

    @Test
    void 관리자는_다른_회원의_예약을_삭제할_수_있다() {
        String adminToken = createToken("admin@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/reservations/4")
                .then().log().all()
                .statusCode(204);
    }

    @Test
    void 삭제된_시간을_다시_등록하면_복구된다() {
        String adminToken = createToken("admin@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(204);

        Map<String, String> params = new HashMap<>();
        params.put("value", "10:00");

        RestAssured.given().log().all()
                .body(params)
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .when().post("/times")
                .then().log().all()
                .statusCode(201);
    }
}
