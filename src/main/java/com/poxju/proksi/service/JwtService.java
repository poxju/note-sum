package com.poxju.proksi.service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.poxju.proksi.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final String secretKey;
    private final long expirationMs;
    
    // Cache the signing key to avoid repeated decoding
    private Key cachedSigningKey;
    
    public JwtService(
            @Value("${JWT_SECRET}") String secretKey,
            @Value("${JWT_EXPIRATION_MS:86400000}") long expirationMs) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs; // Default: 24 hours (86400000 ms)
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }
    public String generateToken(
        HashMap<String, Object> extraClaims,
        UserDetails userDetails
    ){
        return Jwts.builder()
            .claims(extraClaims)
            .subject(((User) userDetails).getEmail())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith((Key)getSignInKey())
            .compact();
    }
    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + expirationMs);

        Key key = (Key)getSignInKey();

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }   

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    
    private Claims extractAllClaims(String token){
        return Jwts.parser()
            .verifyWith((SecretKey) getSignInKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Key getSignInKey() {
        // Cache the key to avoid repeated decoding on every request
        if (cachedSigningKey == null) {
            synchronized (this) {
                if (cachedSigningKey == null) {
                    byte[] keyBytes = Decoders.BASE64.decode(secretKey); // Base64 decode
                    cachedSigningKey = Keys.hmacShaKeyFor(keyBytes);
                }
            }
        }
        return cachedSigningKey;
    }

}
