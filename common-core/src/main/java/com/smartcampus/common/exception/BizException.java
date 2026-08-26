package com.smartcampus.common.exception;

import com.smartcampus.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：由全局异常处理器统一转换为 R 响应。
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = ResultCode.BAD_REQUEST.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
