package com.ecommerce.orders_service.repository;

import com.ecommerce.orders_service.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);
}