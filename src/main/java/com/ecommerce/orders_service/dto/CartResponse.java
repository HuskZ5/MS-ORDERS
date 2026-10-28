package com.ecommerce.orders_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO inmutable (Java Record) que representa el estado detallado y los totales calculados del carrito de compras.
 */
@Schema(description = "Respuesta con el detalle de los productos y desglose monetario del carrito de compras")
public record CartResponse(
        @Schema(
                description = "Identificador único del carrito de compras",
                example = "10",
                type = "integer",
                format = "int64"
        )
        Long cartId,

        @Schema(
                description = "Identificador único del usuario propietario del carrito",
                example = "1",
                type = "integer",
                format = "int64"
        )
        Long userId,

        @Schema(
                description = "Lista de líneas de productos contenidas actualmente en el carrito"
        )
        List<CartItemDto> items,

        @Schema(
                description = "Subtotal acumulado de los productos antes de aplicar descuentos (en BigDecimal)",
                example = "300.00",
                type = "number",
                format = "decimal"
        )
        BigDecimal subtotal,

        @Schema(
                description = "Monto total descontado por cupones promocionales aplicados al carrito (en BigDecimal)",
                example = "30.00",
                type = "number",
                format = "decimal"
        )
        BigDecimal discountAmount,

        @Schema(
                description = "Monto total final a pagar tras aplicar descuentos (en BigDecimal)",
                example = "270.00",
                type = "number",
                format = "decimal"
        )
        BigDecimal totalAmount
) {
    public Long getCartId() { return cartId; }
    public Long getUserId() { return userId; }
    public List<CartItemDto> getItems() { return items; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
