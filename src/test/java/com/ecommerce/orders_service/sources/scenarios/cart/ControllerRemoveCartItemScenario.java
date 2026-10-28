package com.ecommerce.orders_service.sources.scenarios.cart;

/**
 * Record inmutable que modela un escenario de prueba web (MockMvc) para DELETE /api/v1/cart/items/{itemId}.
 */
public record ControllerRemoveCartItemScenario(
        String description,
        Long itemId,
        int expectedHttpStatus,
        Class<? extends Throwable> exceptionClass,
        String expectedErrorCode,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
