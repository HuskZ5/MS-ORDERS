package com.ecommerce.orders_service.exceptions;

public class InvalidCouponException extends RuntimeException {

    public InvalidCouponException(String message) {
        super(message);
    }
}
