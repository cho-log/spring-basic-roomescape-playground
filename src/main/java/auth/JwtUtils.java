package auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class JwtUtils {

    private final SecretKey key;

    public JwtUtils(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createToken(Long memberId, String name, String role) {
        return Jwts.builder()
            .setSubject(memberId.toString())
            .claim("name", name)
            .claim("role", role)
            .signWith(key)
            .compact();
    }

    public TokenPayload parse(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
        return new TokenPayload(
            Long.valueOf(claims.getSubject()),
            claims.get("name", String.class),
            claims.get("role", String.class));
    }
}
