package com.ecommerce.orders_service.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "minimum_purchase", precision = 10, scale = 2)
    private BigDecimal minimumPurchase;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "global_usage_limit")
    private Integer globalUsageLimit;

    @Column(name = "per_user_usage_limit")
    private Integer perUserUsageLimit;

    @Column(name = "stackable", nullable = false)
    private boolean stackable = false;

    public Coupon() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BigDecimal getMinimumPurchase() {
        return minimumPurchase;
    }

    public void setMinimumPurchase(BigDecimal minimumPurchase) {
        this.minimumPurchase = minimumPurchase;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getGlobalUsageLimit() {
        return globalUsageLimit;
    }

    public void setGlobalUsageLimit(Integer globalUsageLimit) {
        this.globalUsageLimit = globalUsageLimit;
    }

    public Integer getPerUserUsageLimit() {
        return perUserUsageLimit;
    }

    public void setPerUserUsageLimit(Integer perUserUsageLimit) {
        this.perUserUsageLimit = perUserUsageLimit;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }
}