package auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;

public class JwtUtils {
    private static final long EXPIRATION_TIME = 1000 * 60 * 60;
    private final String secretKey;

    public JwtUtils(String secretKey) {
        this.secretKey = secretKey;
    }

    public String createToken(Long id, String name, String role) {
        return createToken(id, name, role, EXPIRATION_TIME);
    }

    public String createToken(Long id, String name, String role, long expirationTime) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(id.toString())
                .claim("name", name)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
