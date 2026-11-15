package roomescape.member;

import auth.JwtUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private MemberRepository memberRepository;
    private JwtUtils jwtUtils;

    public MemberService(MemberRepository memberRepository, JwtUtils jwtUtils) {
        this.memberRepository = memberRepository;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public MemberResponse createMember(MemberRequest memberRequest) {
        validateNotDuplicated(memberRequest.getEmail());

        Member member = memberRepository.save(new Member(memberRequest.getName(), memberRequest.getEmail(), memberRequest.getPassword(), Role.USER));
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }

    public String login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword())
                .orElseThrow(() -> new AuthorizationException("이메일 또는 비밀번호가 일치하지 않습니다."));
        return jwtUtils.createToken(member.getId(), member.getName());
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new AuthorizationException("존재하지 않는 회원입니다."));
    }

    private void validateNotDuplicated(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicatedEmailException("이미 가입된 이메일입니다.");
        }
    }
}
