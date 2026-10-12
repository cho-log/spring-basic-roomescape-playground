package roomescape.config;

import auth.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtUtils jwtUtils(@Value("${roomescape.auth.jwt.secret}") String secretKey) { // <-- 여기 키 이름 변경
        return new JwtUtils(secretKey); // 8단계) 프로퍼티 키 수정
    }
}
