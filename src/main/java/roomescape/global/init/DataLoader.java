package roomescape.global.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.Member;
import roomescape.member.MemberRepository;

@Component
@Profile("prod")
public class DataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;

    public DataLoader(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        saveMemberIfAbsent("어드민", "admin@email.com", "password", "ADMIN");
        saveMemberIfAbsent("브라운", "brown@email.com", "password", "USER");
    }

    private void saveMemberIfAbsent(String name, String email, String password, String role) {
        if (!memberRepository.existsByEmail(email)) {
            memberRepository.save(new Member(name, email, password, role));
        }
    }
}
