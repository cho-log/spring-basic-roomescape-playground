package roomescape.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Profile("!test")
@Component
public class DataLoader implements CommandLineRunner {
    private final MemberRepository memberRepository;

    public DataLoader(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        saveIfAbsent("어드민", "admin@email.com", "ADMIN");
        saveIfAbsent("브라운", "brown@email.com", "USER");
    }

    private void saveIfAbsent(String name, String email, String role) {
        if (!memberRepository.existsByEmail(email)) {
            memberRepository.save(new Member(name, email, "password", role));
        }
    }
}
