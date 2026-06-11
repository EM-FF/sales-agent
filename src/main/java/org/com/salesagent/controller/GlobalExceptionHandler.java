package org.com.salesagent.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst().orElse("参数校验失败");
        return ResponseEntity.badRequest().body(msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnknown(Exception e) {
        log.error("未处理异常", e);
        return ResponseEntity.internalServerError().body("服务暂时不可用，请稍后重试");
    }
}
