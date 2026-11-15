package auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class JwtUtils {
    private static final String NAME_CLAIM = "name";

    private final SecretKey secretKey;

    public JwtUtils(String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long memberId, String name) {
        return Jwts.builder()
                .setSubject(memberId.toString())
                .claim(NAME_CLAIM, name)
                .signWith(secretKey)
                .compact();
    }

    public LoginMember getLoginMember(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return new LoginMember(
                    Long.valueOf(claims.getSubject()),
                    claims.get(NAME_CLAIM, String.class)
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.", e);
        }
    }
}
