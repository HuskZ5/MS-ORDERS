package com.ecommerce.orders_service.sources.scenarios.order;

import com.ecommerce.orders_service.model.OrderStatus;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para OrderService.getTodayOrders().
 */
public record GetTodayOrdersScenario(
        String description,
        boolean hasOrders,
        Long orderId,
        Long userId,
        String total,
        OrderStatus status,
        Long productId,
        Integer quantity,
        String unitPrice,
        Integer expectedOrdersCount,
        Class<? extends Throwable> expectedException,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
