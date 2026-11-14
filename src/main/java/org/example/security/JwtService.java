package org.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.exception.InvalidTokenException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String ACCESS = "access";
    public static final String REFRESH = "refresh";

    public record Token(String value, Instant expiresAt) {
    }

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.accessTtl = properties.accessTtl();
        this.refreshTtl = properties.refreshTtl();
    }

    public Token createAccessToken(String username) {
        return create(username, ACCESS, accessTtl);
    }

    public Token createRefreshToken(String username) {
        return create(username, REFRESH, refreshTtl);
    }

    public Claims validateAccessToken(String token) {
        return validate(token, ACCESS);
    }

    public Claims validateRefreshToken(String token) {
        return validate(token, REFRESH);
    }

    private Token create(String username, String type, Duration ttl) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plus(ttl);
        String value = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new Token(value, expiresAt);
    }

    private Claims validate(String token, String type) {
        Jws<Claims> jws;
        try {
            jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Токен недействителен или просрочен");
        }
        Claims claims = jws.getPayload();
        boolean complete = claims.getId() != null && claims.getSubject() != null
                && claims.getIssuedAt() != null && claims.getExpiration() != null;
        if (!"HS256".equals(jws.getHeader().getAlgorithm()) || !complete
                || !type.equals(claims.get("type"))) {
            throw new InvalidTokenException("Токен недействителен или не того типа");
        }
        return claims;
    }
}
