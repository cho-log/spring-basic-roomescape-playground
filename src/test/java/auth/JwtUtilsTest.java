package auth;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {
    private static final String SECRET_KEY = "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=";

    @Test
    void token_contains_member_information() {
        JwtUtils jwtUtils = new JwtUtils(SECRET_KEY, 3_600_000);

        TokenPayload tokenPayload = jwtUtils.parseToken(
                jwtUtils.createToken(1L, "브라운", "brown@email.com", "USER")
        );

        assertThat(tokenPayload.memberId()).isEqualTo(1L);
        assertThat(tokenPayload.name()).isEqualTo("브라운");
        assertThat(tokenPayload.email()).isEqualTo("brown@email.com");
        assertThat(tokenPayload.role()).isEqualTo("USER");
        assertThat(tokenPayload.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void expired_token_is_rejected() {
        JwtUtils jwtUtils = new JwtUtils(SECRET_KEY, -1_000);
        String expiredToken = jwtUtils.createToken(
                1L,
                "브라운",
                "brown@email.com",
                "USER"
        );

        assertThatThrownBy(() -> jwtUtils.parseToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
