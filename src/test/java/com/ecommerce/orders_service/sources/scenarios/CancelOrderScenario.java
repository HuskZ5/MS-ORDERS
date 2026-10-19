package com.ecommerce.orders_service.sources.scenarios;

import com.ecommerce.orders_service.model.OrderStatus;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para la cancelación de pedidos (HU-15),
 * alimentado 100% desde CSV sin valores quemados en el código de pruebas.
 */
public record CancelOrderScenario(
        String description,
        Long orderId,
        boolean orderExists,
        OrderStatus initialStatus,
        Long orderUserId,
        Long requesterUserId,
        String total,
        Long productId,
        Integer quantity,
        String unitPrice,
        boolean shouldSucceed,
        OrderStatus expectedStatus,
        Class<? extends Throwable> expectedException,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
