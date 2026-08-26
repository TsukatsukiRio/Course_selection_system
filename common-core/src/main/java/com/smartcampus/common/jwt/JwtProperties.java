package com.smartcampus.common.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项：签名密钥与有效期。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 签名密钥，长度不少于 32 字节 */
    private String secret = "smart-campus-course-selection-jwt-secret-2026-xwt";

    /** Token 有效期（小时），默认 24 小时 */
    private int expireHours = 24;
}
