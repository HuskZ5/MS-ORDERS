package com.ecommerce.orders_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO inmutable (Java Record) para actualizar la cantidad de un ítem en el carrito.
 */
@Schema(description = "Petición para actualizar la cantidad de un producto específico dentro del carrito de compras")
public record UpdateCartItemQuantityRequest(
        @Schema(
                description = "Nueva cantidad solicitada para el producto en el carrito (mínimo 1, máximo 5 unidades)",
                example = "3",
                type = "integer",
                minimum = "1",
                maximum = "5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Min(value = 1, message = "La cantidad mínima permitida es 1")
        @Max(value = 5, message = "Máximo 5 unidades por producto")
        Integer quantity
) {
}
