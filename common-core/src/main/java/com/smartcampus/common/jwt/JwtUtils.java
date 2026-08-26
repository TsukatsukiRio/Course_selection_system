package com.smartcampus.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：签发与解析 Token（HS256），有效期 24 小时。
 */
@Component
public class JwtUtils {

    private final JwtProperties properties;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 签发 Token，携带 userId、username、role 三个声明。
     */
    public String createToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + properties.getExpireHours() * 3600_000L);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token，签名错误或已过期会抛出异常。
     */
    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 校验 Token 是否合法且未过期。
     */
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return parse(token).get("username", String.class);
    }

    public String getRole(String token) {
        return parse(token).get("role", String.class);
    }

    public Long getUserId(String token) {
        Number userId = parse(token).get("userId", Number.class);
        return userId == null ? null : userId.longValue();
    }
}
