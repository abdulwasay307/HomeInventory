package com.desktopapp.backend.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JWTService {
    private static final String JWT_SECRET = "aojbfoeubU(*@(#&H(@$F*HinIIYN@(#*FH(FGIjbI*@#&*))cwkjbe32#@#)))";
    private static final long JWT_EXPIRY = 24 * 3600000L;
    private static final Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    // .hmacShaKeyFor() converts your secret bytes into a Key object that is compatible with HMAC algorithms like HS256 for signing JWTs.

    // HMAC - symmetric key (same for creation and verfciation), RSA - asymmetric key 
    // SHA - secure hash algorithm

    public static String generateToken(Map<String, Object> payload) {
        return Jwts.builder()
            .setClaims(payload) //payload is map or claims
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRY))
            .signWith(key, SignatureAlgorithm.HS256) 
            .compact();
    }

    public static Claims verifyToken(String token) {
        try {
        return Jwts.parserBuilder()
            .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody(); 
        } catch (Exception e) {
            return null;
        }
    }
}