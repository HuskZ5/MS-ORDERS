package com.ecommerce.orders_service.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    private Long cartId;
    private Long userId;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;

    public CartResponse() {}

    public CartResponse(Long cartId, Long userId, List<CartItemDto> items, BigDecimal totalAmount) {
        this.cartId = cartId;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    // Getters y Setters
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}