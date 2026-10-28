package com.ecommerce.orders_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO inmutable (Java Record) para actualizar la cantidad de un ítem en el carrito.
 */
public record UpdateCartItemQuantityRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Min(value = 1, message = "La cantidad mínima permitida es 1")
        @Max(value = 5, message = "Máximo 5 unidades por producto")
        Integer quantity
) {
}
