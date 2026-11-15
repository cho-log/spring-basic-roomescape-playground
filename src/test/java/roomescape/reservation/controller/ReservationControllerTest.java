package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationControllerTest {

    @Test
    void available_times_are_returned_with_booking_status_in_one_query() {
        ExtractableResponse<Response> response = RestAssured.given()
                .queryParam("date", "2024-03-01")
                .queryParam("themeId", 1)
                .get("/available-times")
                .then()
                .statusCode(200)
                .extract();

        assertThat(response.jsonPath().getList("timeId", Long.class))
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L);
        assertThat(response.jsonPath().getList("booked", Boolean.class))
                .containsExactly(true, false, false, false, false, false);
    }

    @Test
    void admin_can_delete_reservation() {
        String token = createToken("admin@email.com", "password");

        RestAssured.given()
                .cookie("token", token)
                .delete("/reservations/1")
                .then()
                .statusCode(204);

        ExtractableResponse<Response> response = RestAssured.given()
                .cookie("token", token)
                .get("/reservations")
                .then()
                .statusCode(200)
                .extract();
        assertThat(response.jsonPath().getList("id", Long.class)).doesNotContain(1L);
    }

    @Test
    void same_schedule_cannot_be_reserved_twice() {
        String token = createToken("admin@email.com", "password");
        Map<String, String> reservation = Map.of(
                "date", "2024-03-02",
                "time", "1",
                "theme", "1"
        );

        RestAssured.given()
                .body(reservation)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(201);

        ExtractableResponse<Response> duplicateResponse = RestAssured.given()
                .body(reservation)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .post("/reservations")
                .then()
                .statusCode(400)
                .extract();

        assertThat(duplicateResponse.jsonPath().getString("message"))
                .isEqualTo("이미 예약된 날짜, 테마, 시간입니다.");
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
