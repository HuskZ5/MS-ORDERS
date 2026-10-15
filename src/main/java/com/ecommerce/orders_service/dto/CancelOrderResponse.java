package com.ecommerce.orders_service.dto;

import com.ecommerce.orders_service.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para la cancelación exitosa de un pedido.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderResponse {
    private Long orderId;
    private Long userId;
    private OrderStatus estatus;
    private BigDecimal refundAmount;
    private List<ItemReturned> itemsReturned;
    private LocalDateTime cancelledAt;
}
