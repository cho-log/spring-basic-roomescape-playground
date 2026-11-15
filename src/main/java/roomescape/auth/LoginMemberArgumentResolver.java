package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.exception.auth.InvalidAuthenticationException;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final AuthService authService;
    private final CookieTokenExtractor cookieTokenExtractor;

    public LoginMemberArgumentResolver(AuthService authService, CookieTokenExtractor cookieTokenExtractor) {
        this.authService = authService;
        this.cookieTokenExtractor = cookieTokenExtractor;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String token = cookieTokenExtractor.extract(request).orElseThrow(InvalidAuthenticationException::new);

        return authService.findMemberByToken(token);
    }
}
