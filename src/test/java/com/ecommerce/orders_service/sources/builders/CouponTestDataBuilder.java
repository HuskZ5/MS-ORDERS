package com.ecommerce.orders_service.sources.builders;

import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartCoupon;
import com.ecommerce.orders_service.model.Coupon;
import com.ecommerce.orders_service.sources.scenarios.coupon.ApplyCouponScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.CalculateDiscountScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.OrderCouponOperationsScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.ValidateCouponScenario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test Data Builder fluido para construir instancias de {@link Coupon} y {@link CartCoupon}
 * sin constructores manuales ni valores quemados en los métodos de prueba.
 */
public class CouponTestDataBuilder {

    private Long id = 1L;
    private String code = "PROMO10";
    private BigDecimal discountPercentage = new BigDecimal("10.00");
    private LocalDateTime startDate = LocalDateTime.now().minusDays(1);
    private LocalDateTime expirationDate = LocalDateTime.now().plusDays(5);
    private BigDecimal minimumPurchase;
    private boolean active = true;
    private Integer globalUsageLimit;
    private Integer perUserUsageLimit;
    private boolean stackable = false;

    private CouponTestDataBuilder() {}

    public static CouponTestDataBuilder aCoupon() {
        return new CouponTestDataBuilder();
    }

    public CouponTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CouponTestDataBuilder withCode(String code) {
        this.code = code;
        return this;
    }

    public CouponTestDataBuilder withDiscountPercentage(String discountPercentage) {
        if (discountPercentage != null && !discountPercentage.isBlank()) {
            this.discountPercentage = new BigDecimal(discountPercentage);
        }
        return this;
    }

    public CouponTestDataBuilder withStartOffsetDays(Integer offsetDays) {
        if (offsetDays != null) {
            this.startDate = LocalDateTime.now().plusDays(offsetDays);
        }
        return this;
    }

    public CouponTestDataBuilder withExpirationOffsetDays(Integer offsetDays) {
        if (offsetDays != null) {
            this.expirationDate = LocalDateTime.now().plusDays(offsetDays);
        }
        return this;
    }

    public CouponTestDataBuilder withMinimumPurchase(String minimumPurchase) {
        if (minimumPurchase != null && !minimumPurchase.isBlank()) {
            this.minimumPurchase = new BigDecimal(minimumPurchase);
        }
        return this;
    }

    public CouponTestDataBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }

    public CouponTestDataBuilder withGlobalUsageLimit(Integer globalUsageLimit) {
        this.globalUsageLimit = globalUsageLimit;
        return this;
    }

    public CouponTestDataBuilder withPerUserUsageLimit(Integer perUserUsageLimit) {
        this.perUserUsageLimit = perUserUsageLimit;
        return this;
    }

    public CouponTestDataBuilder withStackable(boolean stackable) {
        this.stackable = stackable;
        return this;
    }

    public Coupon build() {
        Coupon coupon = new Coupon();
        coupon.setId(this.id);
        coupon.setCode(this.code);
        coupon.setDiscountPercentage(this.discountPercentage);
        coupon.setStartDate(this.startDate);
        coupon.setExpirationDate(this.expirationDate);
        coupon.setMinimumPurchase(this.minimumPurchase);
        coupon.setActive(this.active);
        coupon.setGlobalUsageLimit(this.globalUsageLimit);
        coupon.setPerUserUsageLimit(this.perUserUsageLimit);
        coupon.setStackable(this.stackable);
        return coupon;
    }

    public static Coupon buildCoupon(ValidateCouponScenario scenario) {
        return aCoupon()
                .withId(scenario.couponId())
                .withCode(scenario.code())
                .withDiscountPercentage(scenario.discountPercentage())
                .withActive(scenario.active())
                .withStartOffsetDays(scenario.startOffsetDays())
                .withExpirationOffsetDays(scenario.expirationOffsetDays())
                .withMinimumPurchase(scenario.minimumPurchase())
                .withGlobalUsageLimit(scenario.globalUsageLimit())
                .withPerUserUsageLimit(scenario.perUserUsageLimit())
                .withStackable(scenario.stackable())
                .build();
    }

    public static Coupon buildCoupon(ApplyCouponScenario scenario) {
        return aCoupon()
                .withId(scenario.couponId())
                .withCode(scenario.couponCode())
                .withDiscountPercentage(scenario.discountPercentage())
                .withStackable(scenario.couponStackable())
                .build();
    }

    public static Cart buildCart(ApplyCouponScenario scenario) {
        CartTestDataBuilder builder = CartTestDataBuilder.aCart()
                .withId(scenario.cartId())
                .withUserId(scenario.userId())
                .withNullItems(scenario.itemsNull());
        if (!scenario.itemsNull() && !scenario.itemsEmpty()) {
            builder.withItem(scenario.productId(), scenario.quantity(), scenario.price());
        }
        return builder.build();
    }

    public static List<CartCoupon> buildExistingAppliedCoupons(ApplyCouponScenario scenario, Cart cart) {
        if (!scenario.hasExistingAppliedCoupon()) {
            return Collections.emptyList();
        }
        Coupon existingCoupon = aCoupon()
                .withId(99L)
                .withCode("EXISTING")
                .withStackable(scenario.existingCouponStackable())
                .build();
        CartCoupon cartCoupon = new CartCoupon();
        cartCoupon.setId(1L);
        cartCoupon.setCart(cart);
        cartCoupon.setCoupon(existingCoupon);
        return List.of(cartCoupon);
    }

    public static List<CartCoupon> buildAppliedCoupons(CalculateDiscountScenario scenario) {
        List<CartCoupon> list = new ArrayList<>();
        if (scenario.firstDiscountPercentage() != null && !scenario.firstDiscountPercentage().isBlank()) {
            CartCoupon cc1 = new CartCoupon();
            cc1.setId(1L);
            cc1.setCoupon(aCoupon().withId(1L).withDiscountPercentage(scenario.firstDiscountPercentage()).build());
            list.add(cc1);
        }
        if (scenario.secondDiscountPercentage() != null && !scenario.secondDiscountPercentage().isBlank()) {
            CartCoupon cc2 = new CartCoupon();
            cc2.setId(2L);
            cc2.setCoupon(aCoupon().withId(2L).withDiscountPercentage(scenario.secondDiscountPercentage()).build());
            list.add(cc2);
        }
        return list;
    }

    public static Coupon buildCoupon(OrderCouponOperationsScenario scenario) {
        if (scenario.couponCode() == null || scenario.couponCode().isBlank()) {
            return null;
        }
        return aCoupon()
                .withId(scenario.couponId())
                .withCode(scenario.couponCode())
                .withDiscountPercentage(scenario.discountPercentage())
                .build();
    }

    public static List<CartCoupon> buildAppliedCoupons(OrderCouponOperationsScenario scenario, Coupon coupon) {
        if (coupon == null) {
            return Collections.emptyList();
        }
        CartCoupon cartCoupon = new CartCoupon();
        cartCoupon.setId(1L);
        cartCoupon.setCoupon(coupon);
        return List.of(cartCoupon);
    }
}
