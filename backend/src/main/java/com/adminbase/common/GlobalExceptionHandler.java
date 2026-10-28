package com.adminbase.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> biz(BizException e) {
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> other(Exception e) {
        log.error("uncaught", e);
        return R.fail("系统开小差了,请稍后重试");
    }
}
