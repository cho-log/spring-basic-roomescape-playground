package roomescape.domain.time;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.concurrency.BeforeCommitBarrier;
import roomescape.global.concurrency.BeforeCommitBarrierConfig;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(BeforeCommitBarrierConfig.class)
public class TimeHttpConcurrencyTest {

    @LocalServerPort
    private int port;

    @Autowired
    private BeforeCommitBarrier beforeCommitBarrier;

    @Autowired
    private TimeRepository timeRepository;

    @BeforeEach
    void setup() {
        RestAssured.port = this.port;
    }

    @Test
    void 동일한_값으로_동시에_시간_생성_요청하면_하나만_201이고_나머지는_409를_응답한다() throws InterruptedException, ExecutionException {
        // given
        String token = createToken("admin@dummy.com", "dummy");

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        Map<String, String> params = new HashMap<>();
        params.put("value", "00:01");

        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();

                return RestAssured.given()
                        .body(params)
                        .cookie("token", token)
                        .contentType(ContentType.JSON)
                        .when().post("/times")
                        .then()
                        .extract().statusCode();
            }));
        }

        // when
        readyLatch.await();
        startLatch.countDown();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then
        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> future : futures) {
            statusCodes.add(future.get());
        }

        assertThat(statusCodes).containsExactlyInAnyOrder(
                HttpStatus.CREATED.value(),
                HttpStatus.CONFLICT.value()
        );
    }

    @Test
    void 동일한_시간을_동시에_삭제_요청하면_500_없이_모두_204를_응답한다() throws InterruptedException, ExecutionException {
        // given
        String token = createToken("admin@dummy.com", "dummy");
        Time savedTime = timeRepository.save(new Time(LocalTime.of(0, 1)));

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<Integer>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();

                return RestAssured.given()
                        .cookie("token", token)
                        .contentType(ContentType.JSON)
                        .when().delete("/times/" + savedTime.getId())
                        .then()
                        .extract().statusCode();
            }));
        }

        // when
        beforeCommitBarrier.arm(threadCount);
        readyLatch.await();
        startLatch.countDown();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        beforeCommitBarrier.disarm();

        // then
        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> future : futures) {
            statusCodes.add(future.get());
        }

        // 두 요청 모두 삭제 대상을 조회한 뒤 커밋 직전에서 만나므로,
        // 늦게 커밋한 쪽은 OptimisticLockingFailureException을 거쳐 204를 응답한다.
        assertThat(statusCodes).containsExactly(HttpStatus.NO_CONTENT.value(), HttpStatus.NO_CONTENT.value());
    }

    private String createToken(String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

        return RestAssured.given()
                .body(params)
                .contentType(ContentType.JSON)
                .when().post("/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("token");
    }
}
