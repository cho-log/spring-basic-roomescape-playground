package auth.support;

import auth.principal.LoginMember;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SessionManager {

    private static final String LOGIN_MEMBER_SESSION_KEY = "LOGIN_MEMBER";

    private final String sessionCookieName;

    public SessionManager(
            String sessionCookieName
    ) {
        this.sessionCookieName = sessionCookieName;
    }

    public void store(HttpServletRequest request, LoginMember loginMember) {
        sessionClear(request);
        request.getSession(true).setAttribute(LOGIN_MEMBER_SESSION_KEY, loginMember);
    }

    public LoginMember extractOrNull(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return (LoginMember) session.getAttribute(LOGIN_MEMBER_SESSION_KEY);
    }

    public void clear(HttpServletRequest request, HttpServletResponse response) {
        sessionClear(request);

        Cookie expiredCookie = new Cookie(sessionCookieName, "");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);
    }

    private void sessionClear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
