package com.ecommerce.orders_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO inmutable (Java Record) para la solicitud de agregar un producto al carrito.
 */
public record AddToCartRequest(
        @NotNull(message = "El ID del usuario es obligatorio")
        @Positive(message = "El ID del usuario debe ser un número positivo")
        Long userId,

        @NotNull(message = "El ID del producto es obligatorio")
        @Positive(message = "El ID del producto debe ser un número positivo")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad solicitada debe ser mayor a cero")
        @Max(value = 5, message = "Máximo 5 unidades por producto")
        Integer quantity
) {
    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
