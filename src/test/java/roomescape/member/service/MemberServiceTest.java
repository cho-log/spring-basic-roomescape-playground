package roomescape.member.service;

import auth.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.member.auth.RevokedTokenStore;
import roomescape.member.domain.LoginMember;
import roomescape.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    private static final String SECRET_KEY = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";

    @Mock
    private MemberRepository memberRepository;

    @Test
    void token_is_resolved_without_member_database_access() {
        JwtUtils jwtUtils = new JwtUtils(SECRET_KEY, 3_600_000);
        MemberService memberService = new MemberService(
                memberRepository,
                jwtUtils,
                new RevokedTokenStore()
        );
        String token = jwtUtils.createToken(
                1L,
                "브라운",
                "brown@email.com",
                "USER"
        );

        LoginMember loginMember = memberService.findLoginMemberByToken(token);

        assertThat(loginMember).isEqualTo(
                new LoginMember(1L, "브라운", "brown@email.com", "USER")
        );
        verifyNoInteractions(memberRepository);
    }
}
