package roomescape.global.config;

import auth.support.LoginMemberArgumentResolver;
import auth.support.SessionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

    @Bean
    public SessionManager sessionManager(@Value("${server.servlet.session.cookie.name}") String sessionCookieName) {
        return new SessionManager(sessionCookieName);
    }

    @Bean
    public LoginMemberArgumentResolver loginMemberArgumentResolver(SessionManager sessionManager) {
        return new LoginMemberArgumentResolver(sessionManager);
    }
}
