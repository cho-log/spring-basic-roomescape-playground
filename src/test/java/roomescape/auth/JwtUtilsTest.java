package roomescape.auth;

import auth.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilsTest {

    private final JwtUtils jwtUtils = new JwtUtils();

    @Test
    void 유효한_토큰이면_로그인_정보를_반환한다() {
        String token = jwtUtils.createToken(1L, "어드민", "ADMIN");
        Claims claims = jwtUtils.getClaims(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("name", String.class)).isEqualTo("어드민");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void 만료된_토큰이면_예외가_발생한다() {
        String expiredToken = jwtUtils.createToken(1L, "어드민", "ADMIN", -1000);

        assertThatThrownBy(() -> jwtUtils.getClaims(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
