package com.ecommerce.orders_service.repository;

import com.ecommerce.orders_service.model.CartCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartCouponRepository extends JpaRepository<CartCoupon, Long> {

    List<CartCoupon> findByCart_Id(Long cartId);

    boolean existsByCart_IdAndCoupon_Id(Long cartId, Long couponId);

    void deleteByCart_Id(Long cartId);
}