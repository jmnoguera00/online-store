package com.onlinestore.infrastructure.adapter.in.web;

import com.onlinestore.domain.exception.PriceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Translates domain exceptions and request binding errors into HTTP responses
 * with a consistent {@link ErrorResponse} body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePriceNotFound(PriceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Required parameter '%s' is missing".formatted(ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Parameter '%s' has an invalid value '%s': expected %s"
                        .formatted(ex.getName(), ex.getValue(), describeExpectedType(ex.getRequiredType())));
    }

    private static String describeExpectedType(Class<?> type) {
        if (type == null) {
            return "a valid value";
        }
        if (LocalDateTime.class.isAssignableFrom(type)) {
            return "an ISO-8601 date-time (e.g. 2020-06-14T16:00:00)";
        }
        if (Number.class.isAssignableFrom(type)) {
            return "a whole number";
        }
        return "a value of type " + type.getSimpleName();
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }
}
