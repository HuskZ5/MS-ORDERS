package com.ecommerce.orders_service.sources.scenarios.cart;

/**
 * Record inmutable que modela un escenario de prueba unitaria para CartService.removeItemFromCart().
 */
public record RemoveCartItemScenario(
        String description,
        Long targetItemId,
        boolean cartFoundByItemId,
        Long cartId,
        Long userId,
        Long existingItemId,
        Long productId,
        Integer existingQuantity,
        String existingPrice,
        Integer expectedRemainingItemsCount,
        Class<? extends Throwable> expectedException,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
