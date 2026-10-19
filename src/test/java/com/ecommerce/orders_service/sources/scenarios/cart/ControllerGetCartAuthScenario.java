package com.ecommerce.orders_service.sources.scenarios.cart;

/**
 * Record inmutable que modela un escenario web para GET /api/carts/me con header X-USER-ID.
 */
public record ControllerGetCartAuthScenario(
        String description,
        Long cartId,
        Long headerUserId,
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
