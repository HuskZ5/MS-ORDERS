package com.ecommerce.orders_service.exceptions;

public class InvalidCartOperationException extends RuntimeException{
    public InvalidCartOperationException(String message) {
        super(message);
    }
}
