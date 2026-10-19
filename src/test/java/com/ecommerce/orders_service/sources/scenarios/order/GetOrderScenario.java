package com.ecommerce.orders_service.sources.scenarios.order;

import com.ecommerce.orders_service.model.OrderStatus;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para la consulta de pedidos por ID (HU-14).
 */
public record GetOrderScenario(
        String description,
        Long orderId,
        boolean orderExists,
        Long userId,
        String total,
        OrderStatus status,
        Long productId,
        Integer quantity,
        String unitPrice,
        Class<? extends Throwable> expectedException,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
