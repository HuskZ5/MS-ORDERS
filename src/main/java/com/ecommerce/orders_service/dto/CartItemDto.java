package com.ecommerce.orders_service.dto;

import java.math.BigDecimal;

public class CartItemDto {
    private Long itemId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;

    public CartItemDto() {}

    public CartItemDto(Long itemId, Long productId, Integer quantity, BigDecimal price) {
        this.itemId = itemId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters y Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}