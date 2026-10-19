package com.ecommerce.orders_service.sources.scenarios.coupon;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para CouponService.validateCoupon().
 */
public record ValidateCouponScenario(
        String description,
        String code,
        Long userId,
        String subtotal,
        boolean couponExists,
        Long couponId,
        String discountPercentage,
        boolean active,
        Integer startOffsetDays,
        Integer expirationOffsetDays,
        String minimumPurchase,
        Integer globalUsageLimit,
        Long globalUsageCount,
        Integer perUserUsageLimit,
        Long userUsageCount,
        boolean stackable,
        Class<? extends Throwable> expectedException,
        String expectedErrorMessage
) {
    @Override
    public String toString() {
        return description;
    }
}
