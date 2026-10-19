package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartCoupon;
import com.ecommerce.orders_service.model.Coupon;
import com.ecommerce.orders_service.model.CouponUsage;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.repository.CartCouponRepository;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.repository.CouponRepository;
import com.ecommerce.orders_service.repository.CouponUsageRepository;
import com.ecommerce.orders_service.sources.builders.CouponTestDataBuilder;
import com.ecommerce.orders_service.sources.builders.OrderTestDataBuilder;
import com.ecommerce.orders_service.sources.scenarios.coupon.ApplyCouponScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.CalculateDiscountScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.OrderCouponOperationsScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.ValidateCouponScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Suite de pruebas unitarias puras para {@link CouponService}.
 * <p>
 * Sigue la regla de arquitectura <b>Zero Hardcoded Data</b>:
 * <ul>
 *   <li>Estructura jerárquica con {@link Nested} y {@link DisplayName}.</li>
 *   <li>Aserciones fluidas exclusivamente con AssertJ ({@code assertThat}).</li>
 *   <li>Uso de {@link CouponTestDataBuilder} y {@link OrderTestDataBuilder} sin operadores {@code new} en el test.</li>
 *   <li>Pruebas parametrizadas alimentadas desde {@code CouponScenarioProvider} mediante {@link MethodSource}.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService - Suite de Pruebas Unitarias (Zero Hardcoded Data)")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private CartCouponRepository cartCouponRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CouponService couponService;

    @Nested
    @DisplayName("Validación de Cupones (validateCoupon)")
    class ValidateCouponTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.CouponScenarioProvider#provideValidateCouponScenarios")
        @DisplayName("Escenarios de validación de cupón")
        void shouldHandleValidateCouponScenarios(ValidateCouponScenario scenario) {

            BigDecimal subtotal = BigDecimal.valueOf(Double.parseDouble(scenario.subtotal()));

            if (scenario.code() != null && !scenario.code().isBlank()) {
                if (scenario.couponExists()) {
                    Coupon coupon = CouponTestDataBuilder.buildCoupon(scenario);
                    when(couponRepository.findByCode(scenario.code().trim())).thenReturn(Optional.of(coupon));

                    if (scenario.globalUsageCount() != null) {
                        lenient().when(couponUsageRepository.countByCoupon_Id(scenario.couponId()))
                                .thenReturn(scenario.globalUsageCount());
                    }
                    if (scenario.userUsageCount() != null) {
                        lenient().when(couponUsageRepository.countByCoupon_IdAndUserId(scenario.couponId(), scenario.userId()))
                                .thenReturn(scenario.userUsageCount());
                    }
                } else {
                    when(couponRepository.findByCode(scenario.code().trim())).thenReturn(Optional.empty());
                }
            }

            if (scenario.expectedException() != null) {
                assertThatThrownBy(() -> couponService.validateCoupon(scenario.code(), scenario.userId(), subtotal))
                        .isInstanceOf(scenario.expectedException())
                        .hasMessage(scenario.expectedErrorMessage());
            } else {
                Coupon result = couponService.validateCoupon(scenario.code(), scenario.userId(), subtotal);

                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(scenario.couponId());
                assertThat(result.getCode()).isEqualTo(scenario.code());
                assertThat(result.getDiscountPercentage()).isEqualByComparingTo(scenario.discountPercentage());
            }
        }
    }

    @Nested
    @DisplayName("Aplicación de Cupones al Carrito (applyCoupon)")
    class ApplyCouponTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.CouponScenarioProvider#provideApplyCouponScenarios")
        @DisplayName("Escenarios de aplicación y acumulación de cupones en carrito")
        void shouldHandleApplyCouponScenarios(ApplyCouponScenario scenario) {

            Cart cart = CouponTestDataBuilder.buildCart(scenario);

            if (scenario.cartExists()) {
                when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.of(cart));

                if (!scenario.itemsNull() && !scenario.itemsEmpty()) {
                    Coupon coupon = CouponTestDataBuilder.buildCoupon(scenario);
                    when(couponRepository.findByCode(scenario.couponCode())).thenReturn(Optional.of(coupon));
                    when(cartCouponRepository.existsByCart_IdAndCoupon_Id(scenario.cartId(), scenario.couponId()))
                            .thenReturn(scenario.alreadyApplied());

                    if (!scenario.alreadyApplied()) {
                        List<CartCoupon> existingCoupons = CouponTestDataBuilder.buildExistingAppliedCoupons(scenario, cart);
                        when(cartCouponRepository.findByCart_Id(scenario.cartId())).thenReturn(existingCoupons);
                    }
                }
            } else {
                when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.empty());
            }

            if (scenario.expectedException() != null) {
                assertThatThrownBy(() -> couponService.applyCoupon(scenario.userId(), scenario.couponCode()))
                        .isInstanceOf(scenario.expectedException())
                        .hasMessage(scenario.expectedErrorMessage());

                verify(cartCouponRepository, never()).save(any(CartCoupon.class));
            } else {
                couponService.applyCoupon(scenario.userId(), scenario.couponCode());

                verify(cartCouponRepository).save(any(CartCoupon.class));
            }
        }
    }

    @Nested
    @DisplayName("Cálculo de Descuentos y Total Final (calculateDiscount / calculateFinalTotal)")
    class CalculateDiscountTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.CouponScenarioProvider#provideCalculateDiscountScenarios")
        @DisplayName("Escenarios de cálculo porcentual y tope de descuento")
        void shouldHandleCalculateDiscountScenarios(CalculateDiscountScenario scenario) {

            List<CartCoupon> appliedCoupons = CouponTestDataBuilder.buildAppliedCoupons(scenario);
            when(cartCouponRepository.findByCart_Id(scenario.cartId())).thenReturn(appliedCoupons);

            BigDecimal subtotal = BigDecimal.valueOf(Double.parseDouble(scenario.subtotal()));

            BigDecimal discount = couponService.calculateDiscount(scenario.cartId(), subtotal);
            BigDecimal finalTotal = couponService.calculateFinalTotal(scenario.cartId(), subtotal);

            assertThat(discount).isEqualByComparingTo(scenario.expectedDiscount());
            assertThat(finalTotal).isEqualByComparingTo(scenario.expectedFinalTotal());
        }
    }

    @Nested
    @DisplayName("Operaciones de Cupones en Órdenes (validateAppliedCouponsForOrder / registerCouponUsages)")
    class OrderCouponOperationsTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.CouponScenarioProvider#provideOrderCouponOperationsScenarios")
        @DisplayName("Escenarios de validación de cupones aplicados y registro de uso en orden")
        void shouldHandleOrderCouponOperationsScenarios(OrderCouponOperationsScenario scenario) {

            Coupon coupon = CouponTestDataBuilder.buildCoupon(scenario);
            List<CartCoupon> appliedCoupons = CouponTestDataBuilder.buildAppliedCoupons(scenario, coupon);

            when(cartCouponRepository.findByCart_Id(scenario.cartId())).thenReturn(appliedCoupons);
            if (coupon != null) {
                when(couponRepository.findByCode(scenario.couponCode())).thenReturn(Optional.of(coupon));
            }

            BigDecimal subtotal = BigDecimal.valueOf(Double.parseDouble(scenario.subtotal()));
            Order order = OrderTestDataBuilder.anOrder()
                    .withId(scenario.orderId())
                    .withUserId(scenario.userId())
                    .withTotal(scenario.subtotal())
                    .build();

            couponService.validateAppliedCouponsForOrder(scenario.cartId(), scenario.userId(), subtotal);
            couponService.registerCouponUsages(scenario.cartId(), order, scenario.userId());

            verify(couponUsageRepository, times(scenario.expectedUsagesSaved())).save(any(CouponUsage.class));
            verify(cartCouponRepository).deleteByCart_Id(scenario.cartId());
        }
    }
}
