package roomescape.domain.theme;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.theme.service.ThemeService;
import roomescape.domain.waiting.entity.ReserveWaiting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ThemeServiceConcurrencyTest {

    @Autowired
    private ThemeService themeService;

    @Test
    void ReserveWaiting을_완전히_같은_레코드로_동시_저장_시_exists_검사를_통과하더라도_DataIntegrityViolationException이_던져진다() throws InterruptedException, ExecutionException {
        // given
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<ReserveWaiting>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Future<ReserveWaiting> future = executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    themeService.saveTheme("Dummy", "It is Dummy for Concurrency Test");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            });
            futures.add(future);
        }

        readyLatch.await();
        startLatch.countDown();

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // then
        int exceptionCount = 0;

        for (Future<ReserveWaiting> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                Throwable rootCause = e.getCause();

                if (rootCause instanceof DataIntegrityViolationException) {
                    exceptionCount++;
                }
            }
        }

        assertThat(exceptionCount).isEqualTo(1);
    }
}
