package com.ecommerce.orders_service.sources.scenarios;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para agregar productos al carrito.
 */
public record AddToCartScenario(
        String description,
        Long cartId,
        Long userId,
        boolean cartExists,
        boolean itemsNull,
        Long existingProductId,
        Integer existingQuantity,
        String existingPrice,
        Long requestProductId,
        Integer requestQuantity,
        String catalogPrice,
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
