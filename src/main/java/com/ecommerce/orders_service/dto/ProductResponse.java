package com.ecommerce.orders_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
@AllArgsConstructor
public class ProductResponse {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    public ProductResponse() {}

}