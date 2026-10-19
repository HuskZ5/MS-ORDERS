package com.ecommerce.orders_service.sources.scenarios;

/**
 * Record inmutable que modela un escenario web para GET /api/carts/{userId}.
 */
public record ControllerGetCartScenario(
        String description,
        Long cartId,
        Long userId,
        Long itemId,
        Long productId,
        Integer quantity,
        String price,
        String totalAmount,
        Integer expectedHttpStatus,
        Class<? extends Throwable> exceptionClass,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
