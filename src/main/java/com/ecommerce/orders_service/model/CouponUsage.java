package com.ecommerce.orders_service.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_usages")
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacion N:1 con Coupon
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    // Relacion N:1 con Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt = LocalDateTime.now();

    public CouponUsage() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}