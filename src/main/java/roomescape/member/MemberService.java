package roomescape.member;

import org.springframework.stereotype.Service;
import auth.JwtUtils;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtUtils jwtUtils;

    public MemberService(MemberRepository memberRepository, JwtUtils jwtUtils) {
        this.memberRepository = memberRepository;
        this.jwtUtils = jwtUtils;
    }

    public MemberResponse createMember(MemberRequest memberRequest) {
        Member member = memberRepository.save(new Member(memberRequest.getName(), memberRequest.getEmail(), memberRequest.getPassword(), Role.USER));
        return new MemberResponse(member.getId(), member.getName(), member.getEmail());
    }

    public String login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmailAndPassword(loginRequest.getEmail(), loginRequest.getPassword())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return jwtUtils.createToken(member.getId(), member.getName(), member.getRole().name());
    }
}
