package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static roomescape.TokenTestSupport.createToken;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TimeExceptionHandlingTest {

    @Test
    void 올바르지_않은_시간을_등록하면_400을_응답한다() {
        String adminToken = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("value", " ");

        RestAssured.given().log().all()
                .body(params)
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .when().post("/times")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 이미_등록된_시간을_등록하면_409를_응답한다() {
        String adminToken = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("value", "10:00");

        RestAssured.given().log().all()
                .body(params)
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .when().post("/times")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 존재하지_않는_시간을_삭제하면_404를_응답한다() {
        String adminToken = createToken("admin@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/times/9999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 일반_회원은_시간을_등록하거나_삭제할_수_없다() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("value", "22:00");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/times")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .when().delete("/times/1")
                .then().log().all()
                .statusCode(403);
    }
}
