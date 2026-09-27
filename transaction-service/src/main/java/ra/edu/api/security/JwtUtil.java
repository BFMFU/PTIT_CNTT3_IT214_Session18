package ra.edu.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${application.jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) { return extractAllClaims(token).getSubject(); }
    public String extractRole(String token) { return extractAllClaims(token).get("role", String.class); }
    public Long extractUserId(String token) { return extractAllClaims(token).get("userId", Long.class); }
    public Long extractCustomerId(String token) { return extractAllClaims(token).get("customerId", Long.class); }

    public boolean isTokenValid(String token) {
        try { return !extractAllClaims(token).getExpiration().before(new Date()); }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }
}
