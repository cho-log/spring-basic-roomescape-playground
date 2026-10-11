package roomescape;

import auth.JwtUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.waiting.WaitingResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class MissionStepTest {

    @Value("${roomescape.auth.jwt.secret}")
    private String secretKey;

    private String createToken(String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        return RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/login")
            .then().log().all()
            .statusCode(200)
            .extract()
            .headers().get("Set-Cookie").getValue().split(";")[0].split("=")[1];
    }

    @Test
    @DisplayName("로그인에 성공하면 토큰이 담긴 쿠키를 응답한다")
    void test_로그인이_성공하면_쿠기헤더에_토큰을_담아서_클라이언트한테_전달() {
        Map<String, String> params = new HashMap<>();
        params.put("email", "admin@email.com");
        params.put("password", "password");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/login")
            .then().log().all()
            .statusCode(200)
            .extract();

        String token = response.headers().get("Set-Cookie").getValue().split(";")[0].split("=")[1];
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("발급받은 토큰으로 로그인한 사용자 정보를 조회한다")
    void test_최초_로그인_이후에_넘겨주는_쿠키헤더에_토큰을_통해_사용자정보를_조회() {
        String token = createToken("admin@email.com", "password");

        ExtractableResponse<Response> checkResponse = RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("token", token)
            .when().get("/login/check")
            .then().log().all()
            .statusCode(200)
            .extract();

        assertThat(checkResponse.body().jsonPath().getString("name")).isEqualTo("어드민");
    }

    @Test
    @DisplayName("예약 생성 시 name이 없으면 쿠키의 로그인 사용자 이름으로 예약된다")
    void test_name이_없으면_로그인_사용자로_예약() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2024-03-02");
        params.put("time", "1");
        params.put("theme", "1");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .cookie("token", token)
            .contentType(ContentType.JSON)
            .when().post("/reservations")
            .then().log().all()
            .extract();

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.as(ReservationResponse.class).getName()).isEqualTo("어드민");
    }

    @Test
    @DisplayName("예약 생성 시 name이 있으면 그 이름으로 예약된다")
    void test_name이_있으면_해당_이름으로_예약() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2024-03-02");
        params.put("time", "1");
        params.put("theme", "1");
        params.put("name", "브라운");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(params)
            .cookie("token", token)
            .contentType(ContentType.JSON)
            .when().post("/reservations")
            .then().log().all()
            .extract();

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.as(ReservationResponse.class).getName()).isEqualTo("브라운");
    }

    @Test
    @DisplayName("어드민 권한이 없으면 어드민 페이지 접근 시 403을 응답한다")
    void test_어드민_권한이_없으면_어드민_페이지_접근_차단() {
        String brownToken = createToken("brown@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    @DisplayName("어드민 권한이 있으면 어드민 페이지에 접근할 수 있다")
    void test_어드민_권한이_있으면_어드민_페이지_접근_승인() {
        String adminToken = createToken("admin@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/admin")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("로그인한 회원이 내 예약 목록을 조회하면 본인이 예약한 예약만 조회된다")
    void test_로그인한_회원의_내_예약_목록_조회() {
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
    @DisplayName("예약이 있는 시간에 대기를 신청하면 내 예약 목록에 1번째 예약대기로 조회된다")
    void test_예약이_있는_시간에_대기_신청_시_내_예약_목록에_대기_순번_조회() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2024-03-01");
        params.put("time", "1");
        params.put("theme", "1");

        WaitingResponse waiting = RestAssured.given().log().all()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then().log().all()
                .statusCode(201)
                .extract().as(WaitingResponse.class);

        List<MyReservationResponse> myReservations = RestAssured.given().log().all()
                .body(params)
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .get("/reservations-mine")
                .then().log().all()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        String status = myReservations.stream()
                .filter(it -> it.getId() == waiting.getId())
                .filter(it -> !it.getStatus().equals("예약"))
                .findFirst()
                .map(it -> it.getStatus())
                .orElse(null);

        assertThat(status).isEqualTo("1번째 예약대기");
    }

    @Test
    @DisplayName("JWT 인증은 외부 패키지로 분리하고, @Component 없이 설정 클래스에서 빈으로 등록한다")
    void test_JwtUtils는_Component_애노테이션_없이_동작() {
        Component componentAnnotation = JwtUtils.class.getAnnotation(Component.class);
        assertThat(componentAnnotation).isNull();
    }

    @Test
    @DisplayName("토큰 생성에 필요한 비밀키는 application.properties에서 가져와서 사용한다")
    void test_비밀키는_properties_파일에서_가져와서_사용() {
        assertThat(secretKey).isNotBlank();
    }
}
