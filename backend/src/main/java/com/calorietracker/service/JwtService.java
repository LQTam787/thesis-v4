package com.calorietracker.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT (JSON Web Token) generation, parsing, and validation.
 * 
 * <p>This service handles all JWT-related operations for the authentication system,
 * using the JJWT library with HS256 (HMAC-SHA256) signing algorithm.</p>
 * 
 * <h2>JWT Structure:</h2>
 * <ul>
 *   <li><b>Header:</b> Algorithm (HS256) and token type (JWT)</li>
 *   <li><b>Payload:</b> Subject (user email), issued at, expiration, custom claims</li>
 *   <li><b>Signature:</b> HMAC-SHA256 signature using secret key</li>
 * </ul>
 * 
 * <h2>Configuration:</h2>
 * <ul>
 *   <li><b>jwt.secret:</b> Base64-encoded secret key (min 256 bits for HS256)</li>
 *   <li><b>jwt.expiration:</b> Token validity duration in milliseconds (default: 24 hours)</li>
 * </ul>
 * 
 * <h2>Security Considerations:</h2>
 * <p>The secret key should be kept secure and rotated periodically in production.
 * Tokens are stateless - revocation requires additional infrastructure like a blacklist.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see com.calorietracker.config.JwtAuthenticationFilter
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
