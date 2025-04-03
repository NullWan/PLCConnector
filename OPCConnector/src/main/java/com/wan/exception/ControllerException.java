package com.wan.exception;

import com.wan.util.RestResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author WanYue
 * @date 2024-08-05
 * @description
 */

@Log4j2
@RestControllerAdvice
public class ControllerException {

    @ExceptionHandler
    public RestResponse<String> exceptionHandler(Exception e) {
        log.error(e);
        return RestResponse.failure(500, e.getMessage());
    }
}
