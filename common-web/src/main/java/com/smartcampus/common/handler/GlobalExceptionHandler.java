package com.smartcampus.common.handler;

import com.smartcampus.common.exception.BizException;
import com.smartcampus.common.result.R;
import com.smartcampus.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：将业务异常、参数校验异常、未知异常统一转换为 R 响应。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数错误" : fieldError.getDefaultMessage();
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "请求参数错误" : fieldError.getDefaultMessage();
        return R.fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return R.fail(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统内部错误", e);
        return R.fail(ResultCode.ERROR);
    }
}
