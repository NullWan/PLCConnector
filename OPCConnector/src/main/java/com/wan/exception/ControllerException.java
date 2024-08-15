package com.wan.exception;

import com.wan.util.RestResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author WanYue
 * @date 2024-08-05
 * @description
 */

@RestControllerAdvice
public class ControllerException {

    @ExceptionHandler
    public RestResponse<String> exceptionHandler(Exception e) {
        return RestResponse.failure(500, e.getMessage());
    }
}
