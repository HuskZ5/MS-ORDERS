package com.ecommerce.orders_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddToCartRequest {
    private Long userId;
    private Long productId;
    private Integer quantity;
    //private BigDecimal price;

    // Getters y Setters
}