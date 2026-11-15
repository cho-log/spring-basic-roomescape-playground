package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservation.MyReservationResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class WaitingConcurrencyTest {

    private static final int REQUEST_COUNT = 10;
    private static final String DATE = "2030-03-01";
    private static final String TIME = "1";
    private static final String THEME = "1";

    @Test
    void 동시에_같은_슬롯에_대기해도_하나만_저장된다() throws InterruptedException {
        String brownToken = createToken("brown@email.com", "password");

        requestWaitingAtOnce(brownToken);

        assertThat(countWaitings(brownToken)).isEqualTo(1);
    }

    private void requestWaitingAtOnce(String token) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(REQUEST_COUNT);

        for (int i = 0; i < REQUEST_COUNT; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    requestWaiting(token);
                } catch (Exception e) {
                    // 실패한 요청은 저장되지 않은 것으로 본다
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await();
        executor.shutdown();
    }

    private void requestWaiting(String token) {
        Map<String, String> params = new HashMap<>();
        params.put("date", DATE);
        params.put("time", TIME);
        params.put("theme", THEME);

        RestAssured.given()
                .body(params)
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .post("/waitings");
    }

    private long countWaitings(String token) {
        List<MyReservationResponse> myReservations = RestAssured.given()
                .cookie("token", token)
                .get("/reservations-mine")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList(".", MyReservationResponse.class);

        return myReservations.stream()
                .filter(it -> !it.status().equals("예약"))
                .count();
    }

    private String createToken(String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        ExtractableResponse<Response> response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/login")
                .then()
                .statusCode(200)
                .extract();

        return response.headers().get("Set-Cookie").getValue().split(";")[0].split("=")[1];
    }
}
