package com.itmo.chgk.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalException {
    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
                return super.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults()
                        .including(ErrorAttributeOptions.Include.MESSAGE));
            }
        };
    }

    @ExceptionHandler(CustomException.class)
    public void handleCustomException(HttpServletResponse response, CustomException ex) throws IOException {
        response.sendError(ex.getStatus().value(), ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorMessage> handleMissingParams(MissingServletRequestParameterException ex) {
        String parameter = ex.getParameterName();

        log.error("{} parametr is missing", parameter);

        return ResponseEntity.status(404)
                .body(new ErrorMessage(String.format("parametr is missing: %s", parameter)));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleMissingParams(MethodArgumentTypeMismatchException ex) {
        String parameter = ex.getParameter().getParameterName();

        log.error("wrong data for parameter : {}", parameter);

        return ResponseEntity.status(404)
                .body(new ErrorMessage(String.format("wrong data for parameter : %s", parameter)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMissingParams(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ex.getMessage();
        return ResponseEntity.status(400)
                .body(new ErrorMessage(message));
    }
}
