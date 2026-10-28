package com.ecommerce.orders_service.sources.scenarios.order;

import com.ecommerce.orders_service.model.OrderStatus;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para la creación de pedidos desde el carrito (HU-03).
 */
public record CreateOrderScenario(
        String description,
        Long userId,
        boolean cartExists,
        boolean cartEmpty,
        Long productId,
        Integer quantity,
        String cartItemPrice,
        String catalogPrice,
        Integer catalogStock,
        String expectedTotal,
        OrderStatus expectedStatus,
        Class<? extends Throwable> stockExceptionClass,
        Class<? extends Throwable> expectedExceptionClass,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
