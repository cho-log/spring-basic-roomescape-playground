package auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

public class MemberSessionManager {
    private static final String LOGIN_MEMBER = "loginMember";
    private final String sessionCookieName;
    private final String sessionCookiePath;

    public MemberSessionManager(String sessionCookieName, String sessionCookiePath) {
        this.sessionCookieName = sessionCookieName;
        this.sessionCookiePath = sessionCookiePath;
    }

    public void login(HttpServletRequest request, LoginMember loginMember) {
        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute(LOGIN_MEMBER, loginMember);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        Cookie cookie = new Cookie(sessionCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath(sessionCookiePath);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public Optional<LoginMember> findLoginMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return Optional.empty();
        }

        LoginMember loginMember = (LoginMember) session.getAttribute(LOGIN_MEMBER);

        return Optional.ofNullable(loginMember);
    }
}
