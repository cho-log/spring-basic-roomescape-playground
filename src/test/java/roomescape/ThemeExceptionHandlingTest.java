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
class ThemeExceptionHandlingTest {

    @Test
    void 올바르지_않은_테마를_등록하면_400을_응답한다() {
        String adminToken = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("name", " ");
        params.put("description", "테마 설명");

        RestAssured.given().log().all()
                .body(params)
                .contentType(ContentType.JSON)
                .cookie("token", adminToken)
                .when().post("/themes")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 존재하지_않는_테마를_삭제하면_404를_응답한다() {
        String adminToken = createToken("admin@email.com", "password");

        RestAssured.given().log().all()
                .cookie("token", adminToken)
                .when().delete("/themes/9999")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 일반_회원은_테마를_등록하거나_삭제할_수_없다() {
        String brownToken = createToken("brown@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("name", "새 테마");
        params.put("description", "테마 설명");

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(403);

        RestAssured.given().log().all()
                .cookie("token", brownToken)
                .when().delete("/themes/1")
                .then().log().all()
                .statusCode(403);
    }
}
