package com.ecommerce.orders_service.sources.scenarios.order;

import com.ecommerce.orders_service.model.OrderStatus;

/**
 * Record inmutable que modela un escenario web para PATCH /api/orders/cancel y PATCH /api/orders/{id}/cancel.
 */
public record ControllerCancelOrderScenario(
        String description,
        Long pathId,
        Long orderId,
        Long userId,
        OrderStatus expectedStatus,
        String refundAmount,
        Long returnedProductId,
        Integer returnedQuantity,
        Integer expectedHttpStatus,
        Class<? extends Throwable> exceptionClass,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
