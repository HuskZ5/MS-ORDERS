package com.ecommerce.orders_service.sources.scenarios.coupon;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para CouponService.applyCoupon().
 */
public record ApplyCouponScenario(
        String description,
        Long userId,
        Long cartId,
        boolean cartExists,
        boolean itemsNull,
        boolean itemsEmpty,
        Long productId,
        Integer quantity,
        String price,
        Long couponId,
        String couponCode,
        String discountPercentage,
        boolean couponStackable,
        boolean alreadyApplied,
        boolean hasExistingAppliedCoupon,
        boolean existingCouponStackable,
        Class<? extends Throwable> expectedException,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
