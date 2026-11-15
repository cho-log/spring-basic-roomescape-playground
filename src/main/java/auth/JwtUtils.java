package auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class JwtUtils {
    private static final String NAME_CLAIM = "name";
    private static final String EMAIL_CLAIM = "email";
    private static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final long expirationMilliseconds;

    public JwtUtils(String secretKey, long expirationMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMilliseconds = expirationMilliseconds;
    }

    public String createToken(Long memberId, String name, String email, String role) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .setSubject(memberId.toString())
                .claim(NAME_CLAIM, name)
                .claim(EMAIL_CLAIM, email)
                .claim(ROLE_CLAIM, role)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(issuedAt.plusMillis(expirationMilliseconds)))
                .signWith(secretKey)
                .compact();
    }

    public TokenPayload parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String subject = claims.getSubject();
        String name = claims.get(NAME_CLAIM, String.class);
        String email = claims.get(EMAIL_CLAIM, String.class);
        String role = claims.get(ROLE_CLAIM, String.class);
        if (subject == null || name == null || email == null || role == null
                || claims.getId() == null || claims.getExpiration() == null) {
            throw new JwtException("필수 사용자 정보가 없는 토큰입니다.");
        }

        try {
            return new TokenPayload(
                    Long.valueOf(subject),
                    name,
                    email,
                    role,
                    claims.getId(),
                    claims.getExpiration().toInstant()
            );
        } catch (NumberFormatException exception) {
            throw new JwtException("사용자 식별자가 올바르지 않은 토큰입니다.", exception);
        }
    }
}
