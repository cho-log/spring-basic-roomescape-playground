package roomescape.config;

import auth.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtUtils jwtUtils(@Value("${roomescape.auth.jwt.secret}") String secretKey) {
        return new JwtUtils(secretKey);
    }
}
