package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberExceptionHandlingTest {

    @Test
    void 회원_필수정보가_없으면_400을_응답한다() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "새 회원");
        params.put("email", "new@email.com");

        RestAssured.given().log().all()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 이미_가입된_이메일로_가입하면_409를_응답한다() {
        Map<String, String> params = new HashMap<>();
        params.put("name", "새 회원");
        params.put("email", "admin@email.com");
        params.put("password", "password");

        RestAssured.given().log().all()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/members")
                .then().log().all()
                .statusCode(409);
    }
}
