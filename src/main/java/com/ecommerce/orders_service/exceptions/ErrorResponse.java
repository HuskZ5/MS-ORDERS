package com.ecommerce.orders_service.exceptions;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String code, String message, String path) {
    public ErrorResponse(int status, String code, String message, String path) {
        this(Instant.now(), status, code, message, path);
    }
}
