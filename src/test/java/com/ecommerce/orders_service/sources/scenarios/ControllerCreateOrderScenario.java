package com.ecommerce.orders_service.sources.scenarios;

/**
 * Record inmutable que modela un escenario web para POST /api/orders (HU-03).
 */
public record ControllerCreateOrderScenario(
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
