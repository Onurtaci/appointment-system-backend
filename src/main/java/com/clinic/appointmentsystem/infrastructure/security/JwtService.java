package com.clinic.appointmentsystem.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {
    private final Key key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expMs) {
        key = Keys.hmacShaKeyFor(secret.getBytes());
        expirationMs = expMs;
    }

    public String generateToken(UUID userId, String email, String role) {
        LocalDateTime now = LocalDateTime.now();
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                .setExpiration(Date.from(now.plus(expirationMs, ChronoUnit.MILLIS).toInstant(ZoneOffset.UTC)))
                .addClaims(Map.of("email", email, "role", role))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
