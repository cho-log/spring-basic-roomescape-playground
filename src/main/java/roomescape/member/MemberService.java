package roomescape.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import auth.JwtUtils;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private MemberRepository memberRepository;
    private JwtUtils jwtUtils;

    public MemberService(JwtUtils jwtUtils, MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public MemberResponse createMember(MemberRequest memberRequest) {
        if (memberRepository.existsByEmail(memberRequest.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member member = memberRepository.save(new Member(memberRequest.getName(), memberRequest.getEmail(), memberRequest.getPassword(), "USER"));
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }

    public String login(String email, String password) {
        Member member = memberRepository.findByEmailAndPassword(email, password).orElseThrow();
        return jwtUtils.createToken(member.getId(), member.getName(), member.getRole());
    }
}
