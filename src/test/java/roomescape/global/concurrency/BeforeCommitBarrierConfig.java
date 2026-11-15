package roomescape.global.concurrency;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

@TestConfiguration
public class BeforeCommitBarrierConfig {

    @Bean
    public BeforeCommitBarrier beforeCommitBarrier() {
        return new BeforeCommitBarrier();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory, BeforeCommitBarrier beforeCommitBarrier) {
        return new JpaTransactionManager(entityManagerFactory) {
            @Override
            protected void prepareForCommit(DefaultTransactionStatus status) {
                // 바깥 트랜잭션의 메소드가 모두 끝나고 실제 커밋을 하기 직전
                if (status.isNewTransaction()) {
                    beforeCommitBarrier.awaitIfArmed();
                }
            }
        };
    }
}
