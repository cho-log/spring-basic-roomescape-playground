package roomescape.auth;

import auth.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import roomescape.exception.auth.InvalidAuthenticationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            MemberRepository memberRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmailAndPassword(
                        loginRequest.email(),
                        loginRequest.password()
                )
                .orElseThrow(InvalidAuthenticationException::new);

        return jwtTokenProvider.createToken(member.getId(), member.getRole().name());
    }

    public LoginMember findMemberByToken(String token) {
        try {
            Long memberId = jwtTokenProvider.extractMemberId(token);

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(InvalidAuthenticationException::new);

            return new LoginMember(member.getId(), member.getName(), member.getRole());
        } catch (JwtException exception) {
            throw new InvalidAuthenticationException();
        }
    }

    public boolean isAdmin(String token) {
        try {
            return Role.ADMIN.name().equals(jwtTokenProvider.extractRole(token));
        } catch (JwtException exception) {
            throw new InvalidAuthenticationException();
        } catch (IllegalArgumentException exception) {
            throw new InvalidAuthenticationException();
        }
    }
}
