package com.smartcampus.gateway.filter;

import com.smartcampus.common.constant.HeaderConstants;
import com.smartcampus.common.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 全局认证过滤器：
 * 1. 白名单接口直接放行；
 * 2. /internal/ 内部接口仅允许带内部标记的服务间调用；
 * 3. 校验 JWT Token，失败返回 401；
 * 4. 按路径前缀做角色校验（admin/teacher），失败返回 403；
 * 5. 将用户信息写入请求头透传给下游微服务。
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/user/auth/login",
            "/api/user/auth/register",
            "/api/course/hot",
            "/api/recommend/hot",
            "/actuator/health",
            "/actuator"
    );

    private final JwtUtils jwtUtils;

    public AuthGlobalFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 静态文档与白名单放行
        if (WHITE_LIST.stream().anyMatch(path::startsWith)
                || path.startsWith("/doc.html")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars")) {
            return chain.filter(exchange);
        }

        // 内部接口防护：仅放行带内部调用标记的请求
        if (path.contains("/internal/")) {
            String internal = request.getHeaders().getFirst(HeaderConstants.X_INTERNAL_CALL);
            if (!"true".equals(internal)) {
                return writeError(exchange, HttpStatus.FORBIDDEN, 403, "禁止访问内部接口");
            }
            return chain.filter(exchange);
        }

        // JWT 鉴权
        String token = resolveToken(request);
        if (token == null || !jwtUtils.validate(token)) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, 401, "未授权，请先登录");
        }
        Claims claims = jwtUtils.parse(token);
        String role = claims.get("role", String.class);

        // 角色校验
        if (path.startsWith("/api/user/admin")
                || path.startsWith("/api/course/admin")
                || path.startsWith("/api/selection/admin")
                || path.startsWith("/api/statistics")) {
            if (!"ADMIN".equals(role)) {
                return writeError(exchange, HttpStatus.FORBIDDEN, 403, "无权限访问，需要管理员权限");
            }
        }
        if (path.startsWith("/api/course/teacher") || path.startsWith("/api/selection/teacher")) {
            if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
                return writeError(exchange, HttpStatus.FORBIDDEN, 403, "无权限访问，需要教师权限");
            }
        }

        // 透传用户信息给下游服务
        ServerHttpRequest mutated = request.mutate()
                .header(HeaderConstants.X_USER_ID, String.valueOf(claims.get("userId")))
                .header(HeaderConstants.X_USERNAME, String.valueOf(claims.get("username")))
                .header(HeaderConstants.X_ROLE, String.valueOf(claims.get("role")))
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private String resolveToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeaders().getFirst("token");
    }

    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
