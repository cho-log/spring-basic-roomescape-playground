package roomescape.config;

import auth.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Bean
    public JwtUtils jwtUtils(@Value("${security.jwt.token.secret-key}") String secretKey) {
        return new JwtUtils(secretKey); // JtUtils는 순수 바자 객체이므로 비밀키 주입
    }
}
