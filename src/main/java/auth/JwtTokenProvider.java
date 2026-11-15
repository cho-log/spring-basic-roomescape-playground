package auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtTokenProvider {

    private final long expirationMilliseconds;
    private final Key secretKey;

    public JwtTokenProvider(
            String secretKey,
            long expirationMilliseconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMilliseconds = expirationMilliseconds;
    }

    public String createToken(Long memberId, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMilliseconds);

        return Jwts.builder()
                .setSubject(memberId.toString())
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractMemberId(String token) {
        return Long.valueOf(extractClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
}
