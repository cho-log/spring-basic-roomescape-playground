package roomescape.global.init;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("prod")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:data-loader-test",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DataLoaderTest {

    @Autowired
    private DataLoader dataLoader;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 운영에_필요한_사용자_정보를_한_번만_초기화한다() {
        dataLoader.run();

        assertThat(memberRepository.count()).isEqualTo(2L);
        assertThat(memberRepository.existsByEmail("admin@email.com")).isTrue();
        assertThat(memberRepository.existsByEmail("brown@email.com")).isTrue();
    }
}
