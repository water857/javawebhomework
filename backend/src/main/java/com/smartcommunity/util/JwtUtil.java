package com.smartcommunity.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class JwtUtil {
    private static String secret;
    private static long expiration;
    private static String issuer;

    static {
        try {
            Properties props = PropertiesUtil.loadProperties("jwt.properties");
            secret = props.getProperty("jwt.secret");
            expiration = Long.parseLong(props.getProperty("jwt.expiration"));
            issuer = props.getProperty("jwt.issuer");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize JWT configuration", e);
        }
    }

    // Generate JWT token
    public static String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim("role", role)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // Validate JWT token and return error type if invalid
    public static String validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return null; // No error, token is valid
        } catch (ExpiredJwtException e) {
            return "Token expired"; // Token has expired
        } catch (MalformedJwtException e) {
            return "Malformed token"; // Token format is invalid
        } catch (SignatureException e) {
            return "Invalid signature"; // Token signature is invalid
        } catch (UnsupportedJwtException e) {
            return "Unsupported token"; // Token type is not supported
        } catch (IllegalArgumentException e) {
            return "Token is empty or contains only whitespace"; // Token is empty
        } catch (Exception e) {
            return "Invalid token"; // Other unspecified errors
        }
    }

    // Get claims from token
    public static Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    // Get username from token
    public static String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // Get role from token
    public static String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    // Check if token is expired
    public static boolean isTokenExpired(String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }
}