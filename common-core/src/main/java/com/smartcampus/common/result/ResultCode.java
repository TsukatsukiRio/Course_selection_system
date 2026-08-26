package com.smartcampus.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码，与需求规格说明的状态码规范保持一致。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),
    ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用");

    private final int code;
    private final String message;
}
