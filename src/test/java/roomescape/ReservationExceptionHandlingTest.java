package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static roomescape.TokenTestSupport.createToken;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationExceptionHandlingTest {

    @Test
    void 예약_필수정보가_없으면_400을_응답한다() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 존재하지_않는_회원으로_예약하면_404를_응답한다() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("name", "존재하지 않는 회원");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "1");

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_시간으로_예약하면_404를_응답한다() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "9999");
        params.put("theme", "1");

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_테마로_예약하면_404를_응답한다() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "9999");

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 같은_날짜와_시간과_테마로_중복_예약하면_409를_응답한다() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "1");

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 존재하지_않는_예약을_삭제하면_404를_응답한다() {
        String adminToken = createToken("admin@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/reservations/9999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 과거_날짜로_예약하면_400을_응답한다() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", LocalDate.now().minusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "1");

        RestAssured.given().log().all()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 다른_회원의_예약을_삭제하면_403을_응답한다() {
        String brownToken = createToken("brown@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(403);
    }

    @Test
    void 일반_회원은_본인_예약만_할_수_있다() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("name", "어드민");
        params.put("date", LocalDate.now().plusDays(1).toString());
        params.put("time", "1");
        params.put("theme", "1");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(403);

        params.remove("name");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    void 전체_예약_목록은_관리자만_조회할_수_있다() {
        String brownToken = createToken("brown@email.com", "password");
        String adminToken = createToken("admin@email.com", "password");

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200);
    }
}
