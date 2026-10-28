package com.ecommerce.orders_service.sources.scenarios.cart;

/**
 * Record inmutable que modela un escenario de prueba web (MockMvc) para PATCH /api/v1/cart/items/{itemId}.
 */
public record ControllerUpdateCartItemQuantityScenario(
        String description,
        Long itemId,
        Integer requestQuantity,
        Long cartId,
        Long userId,
        Long productId,
        String price,
        String totalAmount,
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
