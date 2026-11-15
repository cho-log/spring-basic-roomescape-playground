package roomescape.member;

import auth.LoginMember;
import auth.MemberSessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class MemberController {
    private final MemberService memberService;
    private final MemberSessionManager memberSessionManager;

    public MemberController(MemberService memberService, MemberSessionManager memberSessionManager) {
        this.memberService = memberService;
        this.memberSessionManager = memberSessionManager;
    }

    @PostMapping("/members")
    public ResponseEntity createMember(@RequestBody MemberRequest memberRequest) {
        MemberResponse member = memberService.createMember(memberRequest);
        return ResponseEntity.created(URI.create("/members/" + member.getId())).body(member);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        memberSessionManager.logout(request, response);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        Member member = memberService.login(loginRequest.email(), loginRequest.password());

        LoginMember loginMember = new LoginMember(
                member.getId(),
                member.getName(),
                member.getRole()
        );
        memberSessionManager.login(request, loginMember);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/login/check")
    public ResponseEntity<LoginCheckResponse> checkLogin(
            LoginMember loginMember
    ) {
        return ResponseEntity.ok().body(LoginCheckResponse.from(loginMember));
    }
}
