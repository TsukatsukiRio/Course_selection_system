package com.smartcampus.common.constant;

/**
 * 网关鉴权后向下游服务透传的用户信息请求头。
 */
public final class HeaderConstants {

    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USERNAME = "X-Username";
    public static final String X_ROLE = "X-Role";
    public static final String X_INTERNAL_CALL = "X-Internal-Call";

    private HeaderConstants() {
    }
}
