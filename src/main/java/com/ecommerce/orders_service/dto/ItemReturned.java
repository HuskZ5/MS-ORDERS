package com.ecommerce.orders_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un producto devuelto al cancelar un pedido.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemReturned {
    private Long productId;
    private Integer quantity;
}
