package com.ecommerce.orders_service.exceptions;

public class BusinessOperationException extends RuntimeException{
    public BusinessOperationException(String message) {
        super(message);
    }
}
