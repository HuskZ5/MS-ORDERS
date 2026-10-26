package com.ecommerce.orders_service.dto;

import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.model.ShipmentStatus;

import java.time.LocalDateTime;

public class ShippingResponse {

    private Long shipmentId;
    private Long orderId;
    private ShipmentStatus status;
    private OrderStatus orderStatus;
    private String trackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;

    public ShippingResponse(
            Long shipmentId,
            Long orderId,
            ShipmentStatus status,
            OrderStatus orderStatus,
            String trackingNumber,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String message) {

        this.shipmentId = shipmentId;
        this.orderId = orderId;
        this.status = status;
        this.orderStatus = orderStatus;
        this.trackingNumber = trackingNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.message = message;
    }

    public Long getShipmentId() { return shipmentId; }

    public Long getOrderId() { return orderId; }

    public ShipmentStatus getStatus() { return status; }

    public OrderStatus getOrderStatus() { return orderStatus; }

    public String getTrackingNumber() { return trackingNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getMessage() { return message; }
}
