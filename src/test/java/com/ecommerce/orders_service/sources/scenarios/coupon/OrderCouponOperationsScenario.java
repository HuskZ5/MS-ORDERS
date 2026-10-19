package com.ecommerce.orders_service.sources.scenarios.coupon;

/**
 * Record inmutable que modela un escenario de prueba parametrizado para CouponService.validateAppliedCouponsForOrder() y registerCouponUsages().
 */
public record OrderCouponOperationsScenario(
        String description,
        Long cartId,
        Long orderId,
        Long userId,
        String subtotal,
        Long couponId,
        String couponCode,
        String discountPercentage,
        Integer expectedUsagesSaved
) {
    @Override
    public String toString() {
        return description;
    }
}
