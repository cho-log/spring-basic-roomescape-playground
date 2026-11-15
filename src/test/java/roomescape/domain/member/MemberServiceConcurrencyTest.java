package roomescape.domain.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.member.service.MemberService;
import roomescape.domain.waiting.entity.ReserveWaiting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MemberServiceConcurrencyTest {


    @Autowired
    private MemberService memberService;

    @SpyBean
    private MemberRepository memberRepository;

    @Test
    void Member를_완전히_같은_레코드로_동시_저장_시_exists_검사를_통과하더라도_DataIntegrityViolationException이_던져진다() throws InterruptedException, ExecutionException {
        // given
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // 모든 스레드가 exists 검사를 통과한 뒤에야 save로 넘어가도록 고정
        CyclicBarrier existsCheckedBarrier = new CyclicBarrier(threadCount);
        doAnswer(invocation -> {
            existsCheckedBarrier.await(5, TimeUnit.SECONDS);
            return false;
        }).when(memberRepository).existsByNickname(anyString());

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<ReserveWaiting>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Future<ReserveWaiting> future = executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    memberService.createMember("동시요청", "concurrency@email.com", "concurrency");
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
