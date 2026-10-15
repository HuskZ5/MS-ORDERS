package com.ecommerce.orders_service.model;

/**
 * Enumeración que representa los posibles estados de una orden de compra.
 */
public enum OrderStatus {
    CREATED,
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
