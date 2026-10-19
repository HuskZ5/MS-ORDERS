package com.ecommerce.orders_service.sources.scenarios;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para CartService.getCartByUserId(Long).
 */
public record GetCartByUserIdScenario(
        String description,
        boolean cartExists,
        boolean itemsNull,
        Long cartId,
        Long userId,
        Long productId,
        Integer quantity,
        String price,
        Integer expectedItemsCount,
        String expectedTotalAmount
) {
    @Override
    public String toString() {
        return description;
    }
}
