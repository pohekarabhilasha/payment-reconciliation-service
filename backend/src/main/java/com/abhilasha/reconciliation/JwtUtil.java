package com.abhilasha.reconciliation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // The secret key used to sign tokens. Must be long enough for the algorithm.
    private final SecretKey key = Keys.hmacShaKeyFor(
            "ThisIsASecretKeyForJwtSigningThatMustBeLongEnough123456".getBytes()
    );

    // Token valid for 24 hours (in milliseconds)
    private final long expirationMs = 1000 * 60 * 60 * 24;

    // Create a token for a given username
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    // Read the username out of a token (also verifies the signature)
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    // Check if a token is valid (correct signature and not expired)
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}