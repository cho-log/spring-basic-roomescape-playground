package auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import roomescape.exception.UnauthorizedException;

import java.security.Key;
import java.util.Date;

public class JwtUtils {
    private static final long VALIDITY_MS = 1_800_000L; // 30분

    private final Key key;

    public JwtUtils(String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(Long id, String name, String email, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(id.toString())
                .claim("name", name)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + VALIDITY_MS))
                .signWith(key)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("유효하지 않거나 만료된 토큰입니다.");
        }
    }
}
