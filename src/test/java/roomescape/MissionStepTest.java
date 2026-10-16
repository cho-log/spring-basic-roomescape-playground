package roomescape;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.MemberRepository;
import roomescape.reservation.MyReservationResponse;
import roomescape.reservation.ReservationResponse;
import roomescape.waiting.WaitingResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @Test
    void 일단계() {
        String token = createToken("admin@email.com", "password");
        assertThat(token).isNotBlank();
    }

    @Test
    void 이단계() {
        String token = createToken("admin@email.com", "password");  // 일단계에서 토큰을 추출하는 로직을 메서드로 따로 만들어서 활용하세요.

        Map<String, String> params = new HashMap<>();
        params.put("date", "2024-03-02");
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
        params.put("date", "2024-03-03");

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

        String token = response.headers().get("Set-Cookie").getValue().split(";")[0].split("=")[1];

        return token;
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

    private final long milliseconds = 1000;
    private final String secretKey = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";

    private String createExpiredToken(Long memberId) {
        long now = new Date().getTime();
        Date expiredDate = new Date(now - milliseconds);

        return Jwts.builder()
                .setSubject(memberId.toString())
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .setExpiration(expiredDate)
                .compact();
    }

    @Test
    @DisplayName("만료된 토큰을 요청하면 예외이다.")
    void expiredToken() {
        String expiredToken = createExpiredToken(1L);

        RestAssured.given().log().all()
                .cookie("token", expiredToken)
                .get("/admin")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("토큰이 없는 경우 401을 반환한다.")
    void noToken() {
        RestAssured.given().log().all()
                .get("/admin")
                .then().log().all()
                .statusCode(401);
    }


    @Test
    @DisplayName("잘못된 토큰일 경우 401을 반환한다.")
    void wrongToken() {
        RestAssured.given().log().all()
                .cookie("token", "invalidToken")
                .get("/admin")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("로그인하지 않은 경우 401을 반환한다.")
    void noLogin() {
        RestAssured.given().log().all()
                .get("/login/check")
                .then().log().all()
                .statusCode(401);
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

        Map<String, String> memberParams = new HashMap<>();
        memberParams.put("name", "블랙");
        memberParams.put("email", "black@email.com");
        memberParams.put("password", "password");

        RestAssured.given()
                .body(memberParams)
                .contentType(ContentType.JSON)
                .post("/members")
                .then()
                .statusCode(201);

        String blackToken = createToken("black@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2024-03-01");
        params.put("time", "1");
        params.put("theme", "1");

        // 브라운 대기 생성
        WaitingResponse brownWaiting = RestAssured.given().log().all()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        // 블랙 대기 생성
        WaitingResponse blackWaiting = RestAssured.given().log().all()
                .body(params)
                .cookie("token", blackToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        // 블랙 현재 대기 순위 조회
        List<MyReservationResponse> myReservations = RestAssured.given().log().all()
                .body(params)
                .cookie("token", blackToken)
                .contentType(ContentType.JSON)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        // 블랙이 2번쨰 대기자인지 확인
        String status = myReservations.stream()
                .filter(it -> it.getId().equals(blackWaiting.getId()))
                .filter(it -> !it.getStatus().equals("예약"))
                .findFirst()
                .map(it -> it.getStatus())
                .orElse(null);

        assertThat(status).isEqualTo("2번째 예약대기");

        // 첫 번째 대기자 브라운 취소
        RestAssured.given()
                .cookie("token", brownToken)
                .delete("/waitings/" + brownWaiting.getId())
                .then()
                .statusCode(204);

        // 블랙 대기 순위 다시 조회
        List<MyReservationResponse> afterCancel = RestAssured.given()
                .cookie("token", blackToken)
                .get("/reservations-mine")
                .then()
                .statusCode(200)
                .extract().jsonPath()
                .getList(".", MyReservationResponse.class);

        String afterStatus = afterCancel.stream()
                .filter(it -> it.getId().equals(blackWaiting.getId()))
                .findFirst()
                .map(MyReservationResponse::getStatus)
                .orElse(null);

        assertThat(afterStatus).isEqualTo("1번째 예약대기");
    }

    @Test
    @DisplayName("예약 대기 취소")
    void delete_waiting() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2024-03-01");
        params.put("time", "1");
        params.put("theme", "1");

        // 1. 예약 대기 생성
        WaitingResponse waiting = RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        // 2. 생성한 예약 대기 취소
        RestAssured.given()
                .cookie("token", brownToken)
                .delete("/waitings/" + waiting.getId())
                .then()
                .statusCode(204);

        // 3. 내 예약 목록에서 삭제되었는지 확인
        List<MyReservationResponse> myReservations = RestAssured.given()
                .cookie("token", brownToken)
                .get("/reservations-mine")
                .then()
                .statusCode(200)
                .extract().jsonPath()
                .getList(".", MyReservationResponse.class);

        assertThat(myReservations.stream()
                .noneMatch(it -> it.getStatus().contains("예약대기")))
                .isTrue();
    }

    @Test
    @DisplayName("중복 예약 방지")
    void noTwoReservation() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2026-09-29");
        params.put("time", "1");
        params.put("theme", "1");

        // 예약하기
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(201);

        // 같은 날짜,시간, 테마로 다시 예약하면 실패
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("중복 예약 대기 방지")
    void noTwoWaiting() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2026-09-29");
        params.put("time", "1");
        params.put("theme", "1");

        // 먼저 확정 예약 생성
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(201);

        // 예약 대기 생성
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then()
                .statusCode(201);

        // 같은 회원이 다시 대기 신청하면 실패
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("테마 등록과 삭제는 관리자만 가능하다.")
    void themeAuthorization() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "테스트 테마");
        params.put("description", "테스트 설명");

        String brownToken = createToken("brown@email.com", "password");
        String adminToken = createToken("admin@email.com", "password");

        // 1. 비로그인 등록 실패
        RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .post("/themes")
                .then()
                .statusCode(401);

        // 2. 일반 회원 등록 실패
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/themes")
                .then()
                .statusCode(401);

        // 3. 관리자 등록 성공
        ExtractableResponse<Response> response = RestAssured.given()
                .body(params)
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .post("/themes")
                .then()
                .statusCode(201)
                .extract();

        Long themeId = response.jsonPath().getLong("id");

        // 4. 비로그인 삭제 실패
        RestAssured.given()
                .delete("/themes/" + themeId)
                .then()
                .statusCode(401);

        // 5. 일반 회원 삭제 실패
        RestAssured.given()
                .cookie("token", brownToken)
                .delete("/themes/" + themeId)
                .then()
                .statusCode(401);

        // 6. 삭제가 거절된 뒤에도 테마가 남아있는지 확인
        List<Map<String, Object>> themes = RestAssured.given()
                .get("/themes")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".");

        assertThat(themes.stream()
                .anyMatch(theme -> themeId.equals(
                        ((Number) theme.get("id")).longValue())))
                .isTrue();

        // 기존 예약이 남아 있는지 확인을 위해 삭제할 테마로 예약 생성
        Map<String, String> reservationParams = new HashMap<>();
        reservationParams.put("date", "2026-11-10");
        reservationParams.put("time", "1");
        reservationParams.put("theme", themeId.toString());

        ExtractableResponse<Response> reservationResponse = RestAssured.given()
                .body(reservationParams)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(201)
                .extract();

        Long reservationId = reservationResponse.jsonPath().getLong("id");

        // 7. 관리자 삭제 성공
        RestAssured.given()
                .cookie("token", adminToken)
                .delete("/themes/" + themeId)
                .then()
                .statusCode(204);

        // 8. 삭제된 테마가 목록에서 제외되는지 확인
        List<Map<String, Object>> afterDelete = RestAssured.given()
                .get("/themes")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".");

        // 조건에 맞는 데이터가 하나도 없는지 확인
        assertThat(afterDelete.stream()
                .noneMatch(theme -> themeId.equals(
                        ((Number) theme.get("id")).longValue())))
                .isTrue();

        // 9. 삭제된 테마를 사용하는 기존 예약은 유지되는지 확인
        List<ReservationResponse> reservations = RestAssured.given()
                .get("/reservations")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ReservationResponse.class);

        assertThat(reservations.stream()
                .anyMatch(reservation -> reservation.getId().equals(reservationId)))
                .isTrue();
    }

    @Test
    @DisplayName("시간 등록과 삭제는 관리자만 가능하다.")
    void timeAuthorization() {
        Map<String, String> params = new HashMap<>();
        params.put("value", "23:00");

        String brownToken = createToken("brown@email.com", "password");
        String adminToken = createToken("admin@email.com", "password");

        // 1. 비로그인 등록 실패
        RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .post("/times")
                .then()
                .statusCode(401);

        // 2. 일반 회원 등록 실패
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/times")
                .then()
                .statusCode(401);

        // 3. 관리자 등록 성공
        ExtractableResponse<Response> response = RestAssured.given()
                .body(params)
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .post("/times")
                .then()
                .statusCode(201)
                .extract();

        Long timeId = response.jsonPath().getLong("id");

        // 4. 비로그인 삭제 실패
        RestAssured.given()
                .delete("/times/" + timeId)
                .then()
                .statusCode(401);

        // 5. 일반 회원 삭제 실패
        RestAssured.given()
                .cookie("token", brownToken)
                .delete("/times/" + timeId)
                .then()
                .statusCode(401);

        // 6. 삭제가 거절된 뒤에도 시간이 남아있는지 확인
        List<Map<String, Object>> times = RestAssured.given()
                .get("/times")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".");

        assertThat(times.stream()
                .anyMatch(time -> timeId.equals(
                        ((Number) time.get("id")).longValue())))
                .isTrue();

        // 7. 관리자 삭제 성공
        RestAssured.given()
                .cookie("token", adminToken)
                .delete("/times/" + timeId)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("회원은 본인의 예약만 삭제할 수 있다.")
    void reservationAuthorization() {
        String brownToken = createToken("brown@email.com", "password");
        String adminToken = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2026-10-20");
        params.put("time", "1");
        params.put("theme", "1");

        // 1. brown 예약 생성
        ExtractableResponse<Response> response = RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(201)
                .extract();

        Long reservationId = response.jsonPath().getLong("id");

        // 2. 비로그인 사용자는 삭제 실패
        RestAssured.given()
                .delete("/reservations/" + reservationId)
                .then()
                .statusCode(401);

        // 3. 다른 회원은 삭제 실패
        RestAssured.given()
                .cookie("token", adminToken)
                .delete("/reservations/" + reservationId)
                .then()
                .statusCode(403);

        // 4. 삭제가 거절된 후에도 예약이 남아있는지 확인
        List<ReservationResponse> reservations = RestAssured.given()
                .get("/reservations")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ReservationResponse.class);

        assertThat(reservations.stream()
                .anyMatch(reservation -> reservation.getId().equals(reservationId)))
                .isTrue();

        // 5. 예약한 본인은 삭제 성공
        RestAssured.given()
                .cookie("token", brownToken)
                .delete("/reservations/" + reservationId)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("일반 회원은 다른 이름으로 예약할 수 없다.")
    void memberCannotReserveWithAnotherName() {
        String token = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("name", "다른사람");
        params.put("date", "2026-10-25");
        params.put("time", "1");
        params.put("theme", "1");

        ExtractableResponse<Response> response = RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when()
                .post("/reservations")
                .then()
                .statusCode(201)
                .extract();

        assertThat(response.jsonPath().getString("name"))
                .isEqualTo("브라운");
    }

    @Test
    @DisplayName("관리자는 다른 이름으로 예약할 수 있다.")
    void adminCanReserveWithAnotherName() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("name", "다른사람");
        params.put("date", "2026-10-26");
        params.put("time", "1");
        params.put("theme", "1");

        ExtractableResponse<Response> response = RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when()
                .post("/reservations")
                .then()
                .statusCode(201)
                .extract();

        assertThat(response.jsonPath().getString("name"))
                .isEqualTo("다른사람");
    }

    @Test
    @DisplayName("비밀번호가 누락될 경우 회원가입에 실패한다.")
    void signupWithoutPassword() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "테스트");
        params.put("email", "missing@email.com");

        RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .post("/members")
                .then()
                .statusCode(400);
    }

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("비밀번호가 빈 문자열일 경우 회원가입에 실패한다.")
    void signupWithEmptyPassword() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "테스트");
        params.put("email", "empty@email.com");
        params.put("password", "");

        RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .post("/members")
                .then()
                .statusCode(400);

        assertThat(memberRepository.existsByEmail("empty@email.com"))
                .isFalse();
    }

    @Test
    @DisplayName("관리자 대리 예약은 관리자만 삭제 가능하다.")
    void adminReservationAuthorization() {
        String brownToken = createToken("brown@email.com", "password");
        String adminToken = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("name", "대리예약자");
        params.put("date", "2026-10-11");
        params.put("time", "1");
        params.put("theme", "1");

        // 관리자가 대리 예약 생성
        ExtractableResponse<Response> response = RestAssured.given()
                .body(params)
                .cookie("token", adminToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(201)
                .extract();

        Long reservationId = response.jsonPath().getLong("id");

        // 일반 회원이 삭제하면 403
        RestAssured.given()
                .cookie("token", brownToken)
                .delete("/reservations/" + reservationId)
                .then()
                .statusCode(403);

        // 삭제가 거절되고 예약이 남아 있는지 확인
        List<ReservationResponse> reservations = RestAssured.given()
                .get("/reservations")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", ReservationResponse.class);

        assertThat(reservations.stream()
                .anyMatch(reservation -> reservation.getId().equals(reservationId)))
                .isTrue();

        // 관리자가 삭제하면 204
        RestAssured.given()
                .cookie("token", adminToken)
                .delete("/reservations/" + reservationId)
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("확정 예약이 없는 시간에는 예약 대기를 신청할 수 없다.")
    void waitingWithoutReservation() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2027-01-10");
        params.put("time", "1");
        params.put("theme", "1");

        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("확정 예약이 있는 시간에는 예약 대기를 신청할 수 있다.")
    void waitingWithReservation() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2027-01-11");
        params.put("time", "1");
        params.put("theme", "1");

        // 확정 예약 생성
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(201);

        // 예약이 존재하므로 대기 신청 성공
        RestAssured.given()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then()
                .statusCode(201);
    }
}
