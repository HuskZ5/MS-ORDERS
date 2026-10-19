package com.ecommerce.orders_service.sources.scenarios.coupon;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para CouponService.calculateDiscount() y calculateFinalTotal().
 */
public record CalculateDiscountScenario(
        String description,
        Long cartId,
        String subtotal,
        String firstDiscountPercentage,
        String secondDiscountPercentage,
        String expectedDiscount,
        String expectedFinalTotal
) {
    @Override
    public String toString() {
        return description;
    }
}
