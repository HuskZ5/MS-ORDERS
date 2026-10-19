package com.ecommerce.orders_service.exceptions;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(String message) {
        super(message);
    }
}