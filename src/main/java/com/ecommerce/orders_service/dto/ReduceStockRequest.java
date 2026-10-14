
package com.ecommerce.orders_service.dto;

public class ReduceStockRequest {

    private Integer quantity;

    public ReduceStockRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}

