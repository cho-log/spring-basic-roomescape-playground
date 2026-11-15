package roomescape.global.concurrency;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * arm()으로 지정한 수의 트랜잭션이 모두 커밋 직전에 도달할 때까지 각 트랜잭션의 커밋을 멈춰 세웁니다.<br>
 * 모두 도달하면 한 번에 커밋을 진행시키고, 그 이후의 트랜잭션에는 관여하지 않습니다.<br>
 * 커밋 전에 서로를 기다리는 트랜잭션(같은 unique 값 INSERT, 같은 행 비관적 잠금)은 커밋 직전에 함께 도달할 수 없으므로 사용할 수 없습니다.
 */
public class BeforeCommitBarrier {

    private static final long TIMEOUT_SECONDS = 5;

    private volatile CyclicBarrier barrier;

    public void arm(int transactionCount) {
        barrier = new CyclicBarrier(transactionCount, () -> barrier = null);
    }

    public void disarm() {
        barrier = null;
    }

    void awaitIfArmed() {
        CyclicBarrier current = barrier;
        if (current == null) {
            return;
        }

        try {
            current.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (BrokenBarrierException | TimeoutException e) {
            throw new IllegalStateException("모든 트랜잭션이 커밋 직전에 도달하지 못했습니다.", e);
        }
    }
}
