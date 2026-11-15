package auth.support;

import auth.exception.AuthException;
import auth.principal.LoginMember;
import auth.support.annotation.AdminOnly;
import auth.support.annotation.LoginRequired;
import auth.support.annotation.Public;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class RoleInterceptor implements HandlerInterceptor {

    private final Logger log = LoggerFactory.getLogger(RoleInterceptor.class);
    private final String rootPackage;

    private final SessionManager sessionManager;

    public RoleInterceptor(SessionManager sessionManager, String rootPackage) {
        this.sessionManager = sessionManager;
        this.rootPackage = rootPackage;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        if (!(handlerMethod.getBeanType().getPackageName().startsWith(rootPackage))) {
            return true;
        }

        if (handlerMethod.hasMethodAnnotation(Public.class)) {
            return true;
        }

        boolean adminOnly = handlerMethod.hasMethodAnnotation(AdminOnly.class);
        boolean loginRequired = handlerMethod.hasMethodAnnotation(LoginRequired.class);

        if (!adminOnly && !loginRequired) {
            throw new IllegalArgumentException("API 공개 혹은 인가 정책을 설정하지 않았습니다." + handlerMethod);
        }

        LoginMember loginMember = sessionManager.extractOrNull(request);

        if (loginMember == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (adminOnly && !loginMember.isAdmin()) {
            log.warn("[RoleInterceptor.preHandle] 관리자 권한이 없는 사용자(id={})가 관리자 전용 경로({} {})에 접근을 시도했습니다.",
                    loginMember.id(), request.getMethod(), request.getRequestURI());
            throw new AuthException(HttpStatus.FORBIDDEN, "이 리소스에 접근할 수 있는 권한이 없습니다.");
        }

        return true;
    }
}
