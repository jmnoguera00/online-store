package com.onlinestore.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Error body returned by the REST API.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {

    public static ErrorResponse of(HttpStatus status, String message) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
    }
}
