package com.ecommerce.orders_service.dto;

import com.ecommerce.orders_service.model.ShipmentStatus;

public class ShippingRequest {

    private ShipmentStatus status;
    private String trackingNumber;

    public ShippingRequest() {}

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}
