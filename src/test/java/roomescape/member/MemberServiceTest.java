package roomescape.member;

import auth.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.exception.AuthenticationException;
import roomescape.exception.DuplicateException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, jwtUtils, passwordEncoder);
    }

    @Test
    void 회원을_정상적으로_생성한다() {
        // given
        MemberRequest request = new MemberRequest();
        ReflectionTestUtils.setField(request, "name", "어드민");
        ReflectionTestUtils.setField(request, "email", "admin@email.com");
        ReflectionTestUtils.setField(request, "password", "1234");

        Member savedMember = new Member(1L, "어드민", "admin@email.com", "USER");
        given(passwordEncoder.encode(anyString())).willReturn("encoded-password");
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // when
        MemberResponse response = memberService.createMember(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("어드민");
    }

    @Test
    void 이메일이_중복되면_예외가_발생한다() {
        // given
        MemberRequest request = new MemberRequest();
        ReflectionTestUtils.setField(request, "name", "어드민");
        ReflectionTestUtils.setField(request, "email", "admin@email.com");
        ReflectionTestUtils.setField(request, "password", "1234");

        given(memberRepository.existsByEmail("admin@email.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 가입되어있는 이메일입니다.");
    }

    @Test
    void 이메일과_비밀번호가_일치하면_토큰을_발급한다() {
        // given
        String email = "admin@email.com";
        String password = "password";
        Member member = new Member(1L, "어드민", email, "ADMIN");

        given(memberRepository.findByEmail(email)).willReturn(Optional.of(member));
        given(passwordEncoder.matches(password, member.getPassword())).willReturn(true);
        given(jwtUtils.createToken(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        )).willReturn("mocked-jwt-token");

        // when
        String token = memberService.login(email, password);

        // then
        assertThat(token).isEqualTo("mocked-jwt-token");
    }

    @Test
    void 로그인_정보가_일치하지_않으면_AuthenticationException이_발생한다() {
        // given
        String wrongEmail = "wrong@email.com";
        String wrongPassword = "wrong-password";
        given(memberRepository.findByEmail(wrongEmail)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.login(wrongEmail, wrongPassword))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
