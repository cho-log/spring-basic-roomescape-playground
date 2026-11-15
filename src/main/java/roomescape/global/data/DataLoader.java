package roomescape.global.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.entity.Member;
import roomescape.domain.member.repository.MemberRepository;

@Component
@Profile("prod")
public class DataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;

    public DataLoader(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    @Transactional
    @Override
    public void run(String... args) throws Exception {
        Member admin = new Member("어드민", "admin@email.com", "password", "ADMIN");
        Member brown = new Member("브라운", "brown@email.com", "password", "USER");

        memberRepository.save(admin);
        memberRepository.save(brown);
    }
}
