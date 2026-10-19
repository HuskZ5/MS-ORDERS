package com.ecommerce.orders_service.sources.scenarios.cart;

/**
 * Record inmutable que modela un escenario web para POST /api/carts/{userId}/coupons.
 */
public record ControllerApplyCouponScenario(
        String description,
        Long cartId,
        Long userId,
        String couponCode,
        Long itemId,
        Long productId,
        Integer quantity,
        String price,
        String subtotal,
        String discountAmount,
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
