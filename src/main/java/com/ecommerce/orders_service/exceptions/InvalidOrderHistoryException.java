package com.ecommerce.orders_service.exceptions;

public class InvalidOrderHistoryException extends RuntimeException {

    public InvalidOrderHistoryException(String message) {
        super(message);
    }
}
