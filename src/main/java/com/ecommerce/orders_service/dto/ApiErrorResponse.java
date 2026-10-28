package com.ecommerce.orders_service.dto;

import java.time.Instant;

/**
 * Formato estándar de respuesta de error para toda la API del microservicio MS-03 Órdenes.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path
) {
}
