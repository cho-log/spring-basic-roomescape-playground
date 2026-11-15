package roomescape.member.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberAuthenticationTest {

    @Test
    void admin_apis_require_admin_authentication() {
        String memberToken = createToken("brown@email.com", "password");
        List<AdminApiRequest> adminApiRequests = List.of(
                new AdminApiRequest("GET", "/reservations", null),
                new AdminApiRequest("DELETE", "/reservations/1", null),
                new AdminApiRequest("POST", "/themes", Map.of("name", "theme", "description", "description")),
                new AdminApiRequest("DELETE", "/themes/1", null),
                new AdminApiRequest("POST", "/times", Map.of("value", "23:00")),
                new AdminApiRequest("DELETE", "/times/1", null)
        );

        for (AdminApiRequest request : adminApiRequests) {
            assertUnauthorized(request, null);
            assertUnauthorized(request, memberToken);
        }
    }

    @Test
    void logged_out_token_cannot_be_reused() {
        String token = createToken("brown@email.com", "password");

        RestAssured.given()
                .cookie("token", token)
                .post("/logout")
                .then()
                .statusCode(200);

        RestAssured.given()
                .cookie("token", token)
                .get("/login/check")
                .then()
                .statusCode(400);
    }

    private void assertUnauthorized(AdminApiRequest request, String token) {
        var requestSpecification = RestAssured.given()
                .contentType(ContentType.JSON);
        if (request.body() != null) {
            requestSpecification.body(request.body());
        }
        if (token != null) {
            requestSpecification.cookie("token", token);
        }

        requestSpecification
                .request(request.method(), request.path())
                .then()
                .statusCode(401);
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

    private record AdminApiRequest(String method, String path, Object body) {
    }
}
