package roomescape.waiting.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingControllerTest {

    @Test
    void required_fields_are_validated_by_request_dto() {
        String token = createToken("brown@email.com", "password");

        String message = RestAssured.given()
                .body(Map.of("date", "2024-03-01", "theme", "1"))
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .post("/waitings")
                .then()
                .statusCode(400)
                .extract().jsonPath().getString("message");

        assertThat(message).isEqualTo("예약 날짜, 테마, 시간은 필수입니다.");
    }

    private String createToken(String email, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .headers()
                .get("Set-Cookie")
                .getValue()
                .split(";")[0]
                .split("=")[1];
    }
}
