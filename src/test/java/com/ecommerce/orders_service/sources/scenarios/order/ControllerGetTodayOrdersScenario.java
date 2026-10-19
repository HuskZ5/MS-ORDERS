package com.ecommerce.orders_service.sources.scenarios.order;

/**
 * Record inmutable que modela un escenario web para GET /api/orders/today.
 */
public record ControllerGetTodayOrdersScenario(
        String description,
        Long orderId,
        Long userId,
        String total,
        String status,
        Long itemId,
        Long productId,
        Integer quantity,
        String unitPrice,
        Integer expectedHttpStatus,
        Class<? extends Throwable> exceptionClass,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
