package roomescape.member;

import auth.LoginMember;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.global.config.AuthConfig;
import roomescape.member.exception.MemberErrorCode;
import roomescape.member.exception.MemberException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(AuthConfig.class)
@WebMvcTest(MemberController.class)
class MemberControllerTest {
    private static final String EMAIL = "admin@email.com";
    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    void 로그인_요청이_성공하면_200을_반환하고_로그인_회원_정보를_세션에_저장한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        Member member = new Member(1L, "어드민", EMAIL, "ADMIN");
        given(memberService.login(EMAIL, PASSWORD)).willReturn(member);

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("loginMember", new LoginMember(1L, "어드민", "ADMIN")));

        then(memberService).should().login(request.email(), request.password());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void 이메일이_비어있으면_400을_반환한다(String email) throws Exception {
        // given
        LoginRequest request = new LoginRequest(email, PASSWORD);

        // when & then
        assertBadLoginRequest(request, "이메일은 비어 있을 수 없습니다.");
        then(memberService).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void 비밀번호가_비어있으면_400을_반환한다(String password) throws Exception {
        // given
        LoginRequest request = new LoginRequest(EMAIL, password);

        // when & then
        assertBadLoginRequest(request, "비밀번호는 비어 있을 수 없습니다.");
        then(memberService).shouldHaveNoInteractions();
    }

    @Test
    void 이메일_형식이_올바르지_않으면_400을_반환한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest("invalid", PASSWORD);

        // when & then
        assertBadLoginRequest(request, "이메일 형식이 올바르지 않습니다.");
        then(memberService).shouldHaveNoInteractions();
    }

    @Test
    void 서비스에서_로그인_실패_예외가_발생하면_401을_반환한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest(EMAIL, "wrong-password");
        given(memberService.login(request.email(), request.password())).willThrow(new MemberException(MemberErrorCode.LOGIN_FAILED));

        // when & then
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MEMBER_LOGIN_FAILED"))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
        then(memberService).should().login(request.email(), request.password());
    }

    @Test
    void 세션의_회원_이름을_서비스_호출_없이_반환한다() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", new LoginMember(1L, "어드민", "ADMIN"));

        // when & then
        mockMvc.perform(get("/login/check").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"name\":\"어드민\"}", true));
        then(memberService).shouldHaveNoInteractions();
    }

    @Test
    void 세션이_없으면_로그인_필요_오류를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/login/check"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MEMBER_LOGIN_REQUIRED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
        then(memberService).shouldHaveNoInteractions();
    }

    @Test
    void 세션에_로그인_회원_정보가_없으면_로그인_필요_오류를_반환한다() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();

        // when & then
        mockMvc.perform(get("/login/check").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("MEMBER_LOGIN_REQUIRED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));
        then(memberService).shouldHaveNoInteractions();
    }

    private void assertBadLoginRequest(LoginRequest request, String message) throws Exception {
        mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GLOBAL_BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(containsString(message)));
    }
}
