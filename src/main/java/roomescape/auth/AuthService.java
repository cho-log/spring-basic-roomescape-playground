package roomescape.auth;

import org.springframework.stereotype.Service;
import roomescape.member.Member;
import roomescape.member.MemberRepository;

@Service
public class AuthService {
    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member authenticateMember(LoginRequest loginRequest) {
        return memberRepository.findByEmailAndPassword(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        ).orElseThrow(
                () -> new RuntimeException("존재하지 않는 회원입니다.")
        );
    }
}
