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

    // 生成 JWT 令牌
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

    // 校验 JWT 令牌并在无效时返回错误类型
    public static String validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return null; // 无错误，令牌有效
        } catch (ExpiredJwtException e) {
            return "Token expired"; // 令牌已过期
        } catch (MalformedJwtException e) {
            return "Malformed token"; // 令牌格式不正确
        } catch (SignatureException e) {
            return "Invalid signature"; // 令牌签名无效
        } catch (UnsupportedJwtException e) {
            return "Unsupported token"; // 不支持的令牌类型
        } catch (IllegalArgumentException e) {
            return "Token is empty or contains only whitespace"; // 令牌为空或仅包含空白字符
        } catch (Exception e) {
            return "Invalid token"; // 其他未知错误
        }
    }

    // 从令牌中获取声明
    public static Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    // 从令牌中获取用户名
    public static String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    // 从令牌中获取角色
    public static String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    // 检查令牌是否过期
    public static boolean isTokenExpired(String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }
}
