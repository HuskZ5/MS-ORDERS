package com.ecommerce.orders_service.repository;

import com.ecommerce.orders_service.model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    long countByCoupon_Id(Long couponId);

    long countByCoupon_IdAndUserId(Long couponId, Long userId);

    boolean existsByOrder_IdAndCoupon_Id(Long orderId, Long couponId);
}