package roomescape.global.config;

import auth.MemberSessionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

    @Bean
    public MemberSessionManager memberSessionManager(
            @Value("${server.servlet.session.cookie.name}") String sessionCookieName,
            @Value("${server.servlet.session.cookie.path:/}") String sessionCookiePath
    ) {
        return new MemberSessionManager(sessionCookieName, sessionCookiePath);
    }
}
