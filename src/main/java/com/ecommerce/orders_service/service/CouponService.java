package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.exceptions.CouponNotFoundException;
import com.ecommerce.orders_service.exceptions.InvalidCouponException;
import com.ecommerce.orders_service.model.Coupon;
import com.ecommerce.orders_service.repository.CouponRepository;
import com.ecommerce.orders_service.repository.CouponUsageRepository;
import com.ecommerce.orders_service.repository.CartCouponRepository;
import com.ecommerce.orders_service.model.CartCoupon;
import com.ecommerce.orders_service.model.CouponUsage;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.exceptions.CartEmptyException;
import com.ecommerce.orders_service.exceptions.CartNotFoundException;
import com.ecommerce.orders_service.model.Cart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.math.RoundingMode;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CartCouponRepository cartCouponRepository;
    private final CartRepository cartRepository;

    public CouponService(
            CouponRepository couponRepository,
            CouponUsageRepository couponUsageRepository,
            CartCouponRepository cartCouponRepository,
            CartRepository cartRepository
    ){
        this.couponRepository = couponRepository;
        this.couponUsageRepository = couponUsageRepository;
        this.cartCouponRepository = cartCouponRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional(readOnly = true)
    public Coupon validateCoupon(String code, Long userId, BigDecimal subtotal){

        if (code == null || code.isBlank()) {
            throw new InvalidCouponException("Debes proporcionar un código de cupón para aplicarlo");
        }

        Coupon coupon = couponRepository.findByCode(code.trim())
                .orElseThrow(()->
                        new CouponNotFoundException("El cupón no existe"));
        if (!coupon.isActive()) {
            throw new InvalidCouponException("El cupón se encuentra inactivo");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(coupon.getStartDate())) {
            throw new InvalidCouponException("El cupón todavía no está vigente");
        }

        if (now.isAfter(coupon.getExpirationDate())) {
            throw new InvalidCouponException("El cupón ha expirado");

        }

        if (coupon.getMinimumPurchase() != null && subtotal.compareTo(coupon.getMinimumPurchase()) < 0) {
            throw new InvalidCouponException("El carrito no alcanza la compra mínima requerida de $" + coupon.getMinimumPurchase());
        }

        if (coupon.getGlobalUsageLimit() != null) {
            long globalUsageCount = couponUsageRepository.countByCoupon_Id(coupon.getId());

            if (globalUsageCount >= coupon.getGlobalUsageLimit()){
                throw new InvalidCouponException("El cupón ha alcanzado su límite global de usos");
            }
        }

        if (coupon.getPerUserUsageLimit() != null) {
            long userUsageCount = couponUsageRepository.countByCoupon_IdAndUserId(
                    coupon.getId(),
                    userId
            );

            if (userUsageCount >= coupon.getPerUserUsageLimit()){
                throw new InvalidCouponException("El usuario ha alcanzado el límite de usos permitidos para este cupón");
            }
        }
        return coupon;
    }

    @Transactional
    public void applyCoupon(Long userId, String code) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new CartNotFoundException(
                                "No se encontró carrito para el usuario " + userId
                        ));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartEmptyException(
                    "No se puede aplicar un cupón a un carrito vacío"
            );
        }

        BigDecimal subtotal = cart.getItems().stream()
                .map(item ->
                        item.getPrice().multiply(
                                BigDecimal.valueOf(item.getQuantity())
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Coupon coupon = validateCoupon(code, userId, subtotal);

        validateDuplicateCoupon(cart.getId(), coupon);

        validateStackableCoupons(cart.getId(), coupon);
        CartCoupon cartCoupon = new CartCoupon();
        cartCoupon.setCart(cart);
        cartCoupon.setCoupon(coupon);

        cartCouponRepository.save(cartCoupon);

    }

    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(Long cartId, BigDecimal subtotal) {

        List<CartCoupon> appliedCoupons =
                cartCouponRepository.findByCart_Id(cartId);

        BigDecimal totalDiscount = appliedCoupons.stream()
                .map(cartCoupon -> {
                    BigDecimal percentage =
                            cartCoupon.getCoupon().getDiscountPercentage();

                    return subtotal
                            .multiply(percentage)
                            .divide(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDiscount.compareTo(subtotal) > 0) {
            return subtotal;
        }

        return totalDiscount;
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateFinalTotal(Long cartId, BigDecimal subtotal) {

        BigDecimal discount = calculateDiscount(cartId, subtotal);

        return subtotal.subtract(discount);
    }

    @Transactional(readOnly = true)
    public void validateAppliedCouponsForOrder(
            Long cartId,
            Long userId,
            BigDecimal subtotal
    ) {
        List<CartCoupon> appliedCoupons =
                cartCouponRepository.findByCart_Id(cartId);

        for (CartCoupon cartCoupon : appliedCoupons) {
            Coupon coupon = cartCoupon.getCoupon();

            validateCoupon(
                    coupon.getCode(),
                    userId,
                    subtotal
            );
        }
    }

    @Transactional
    public void registerCouponUsages(
            Long cartId,
            Order order,
            Long userId
    ) {
        List<CartCoupon> appliedCoupons =
                cartCouponRepository.findByCart_Id(cartId);

        for (CartCoupon cartCoupon : appliedCoupons) {
            CouponUsage usage = new CouponUsage();

            usage.setCoupon(cartCoupon.getCoupon());
            usage.setOrder(order);
            usage.setUserId(userId);
            usage.setUsedAt(LocalDateTime.now());

            couponUsageRepository.save(usage);
        }

        cartCouponRepository.deleteByCart_Id(cartId);
    }

    private void validateDuplicateCoupon(Long cartId, Coupon coupon) {

        boolean alreadyApplied =
                cartCouponRepository.existsByCart_IdAndCoupon_Id(
                        cartId,
                        coupon.getId()
                );

        if (alreadyApplied) {
            throw new InvalidCouponException(
                    "El cupón ya se encuentra aplicado al carrito"
            );
        }
    }

    private void validateStackableCoupons(Long cartId, Coupon coupon) {
        List<CartCoupon> appliedCoupons =
                cartCouponRepository.findByCart_Id(cartId);

        if (appliedCoupons.isEmpty()){
            return;
        }
        if (!coupon.isStackable()) {
            throw new InvalidCouponException(
                    "El cupón no puede acumularse con otros cupones"
            );
        }

        boolean hasNonStackableCoupon = appliedCoupons.stream()
                .anyMatch(cartCoupon -> !cartCoupon.getCoupon().isStackable());

        if (hasNonStackableCoupon) {
            throw new InvalidCouponException(
                    "El carrito contiene un cupón que no permite acumulación"
            );
        }
    }

}
