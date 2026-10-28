package com.ecommerce.orders_service.sources.scenarios.cart;

/**
 * Record inmutable que modela un escenario de prueba unitaria para CartService.updateItemQuantity().
 */
public record UpdateCartItemQuantityScenario(
        String description,
        Long targetItemId,
        Integer requestQuantity,
        boolean cartFoundByItemId,
        Long cartId,
        Long userId,
        Long existingItemId,
        Long productId,
        Integer existingQuantity,
        String existingPrice,
        String catalogPrice,
        Integer catalogStock,
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
