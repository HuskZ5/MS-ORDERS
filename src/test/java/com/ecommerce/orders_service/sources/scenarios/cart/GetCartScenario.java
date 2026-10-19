package com.ecommerce.orders_service.sources.scenarios.cart;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para CartService.getCarByAuthenticatedUser().
 */
public record GetCartScenario(
        String description,
        boolean cartExists,
        Long cartId,
        Long userId,
        Long productId,
        Integer quantity,
        String price,
        Integer expectedItemsCount,
        String expectedTotalAmount,
        Class<? extends Throwable> expectedException,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
