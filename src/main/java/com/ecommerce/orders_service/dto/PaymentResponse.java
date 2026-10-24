
package com.ecommerce.orders_service.dto;

import com.ecommerce.orders_service.model.PaymentStatus;
import com.ecommerce.orders_service.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private String operationId;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private String message;

    public PaymentResponse() {}

    public PaymentResponse(
            Long paymentId,
            Long orderId,
            String operationId,
            BigDecimal amount,
            PaymentStatus paymentStatus,
            OrderStatus orderStatus,
            LocalDateTime createdAt,
            String message) {

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.operationId = operationId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
        this.message = message;
    }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public OrderStatus getOrderStatus() { return orderStatus; }
    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
