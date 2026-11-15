package roomescape.config;

import auth.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {

    @Bean
    public JwtTokenProvider jwtTokenProvider(
            @Value("${roomescape.auth.jwt.secret}") String secretKey,
            @Value("${roomescape.auth.jwt.expiration-milliseconds}") long expirationMilliseconds
    ) {
        return new JwtTokenProvider(secretKey, expirationMilliseconds);
    }
}
