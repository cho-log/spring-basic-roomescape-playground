package roomescape.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import roomescape.member.Member;
import roomescape.member.MemberRepository;

// data.sql 대신 초기 데이터를 넣어주는 역할
@Profile("!test") // 중요) test 프로파일이 '아닐 때만' 동작
@Component
public class DataLoader implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String password = passwordEncoder.encode("password");

        // 운영 초기 데이터로 어드민과 브라운 회원 등록
        memberRepository.save(new Member("어드민", "admin@email.com", password, "ADMIN"));
        memberRepository.save(new Member("브라운", "brown@email.com", password, "USER"));
    }
}
