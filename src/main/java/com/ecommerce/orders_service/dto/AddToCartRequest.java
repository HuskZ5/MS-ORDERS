package com.ecommerce.orders_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddToCartRequest {
    private Long userId;
    @NotNull(message = "El Id del producto es obligatorio")
    @Positive(message = "El id del producto debe ser mayor a cero")
    private Long productId;
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad solicitada debe ser mayor a cero")
    private Integer quantity;

    public Long userId() {
        return userId;
    }

    public Long productId() {
        return productId;
    }

    public Integer quantity() {
        return quantity;
    }
}
