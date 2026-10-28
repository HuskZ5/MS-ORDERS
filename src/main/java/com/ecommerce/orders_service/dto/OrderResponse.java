package com.ecommerce.orders_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO inmutable (Java Record) que representa una orden de compra confirmada con su desglose histórico.
 */
@Schema(description = "Respuesta con la información de una orden de compra generada y sus líneas históricas")
public record OrderResponse(
        @Schema(
                description = "Identificador único de la orden de compra",
                example = "100",
                type = "integer",
                format = "int64"
        )
        Long id,

        @Schema(
                description = "Identificador único del usuario que realizó la orden",
                example = "1",
                type = "integer",
                format = "int64"
        )
        Long userId,

        @Schema(
                description = "Monto total final de la orden calculado en el backend con precios vigentes (en BigDecimal)",
                example = "270.00",
                type = "number",
                format = "decimal"
        )
        BigDecimal total,

        @Schema(
                description = "Estado actual del ciclo de vida de la orden",
                example = "CREATED",
                type = "string",
                allowableValues = {"CREATED", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"}
        )
        String status,

        @Schema(
                description = "Fecha y hora de creación de la orden en formato ISO-8601",
                example = "2026-10-27T20:15:30",
                type = "string",
                format = "date-time"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Detalle histórico de los productos comprados con su cantidad y precio unitario congelado"
        )
        List<OrderItemDto> items
) {
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getTotal() { return total; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItemDto> getItems() { return items; }
}
