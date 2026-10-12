package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.OrderItemDto;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Obtener los detalles de una orden por su ID (HU-14/GET Order)
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        return mapToOrderResponse(order);
    }

    /**
     * Mapear la entidad Order a OrderResponse DTO
     */
    public OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems() != null ?
                order.getItems().stream()
                        .map(item -> new OrderItemDto(
                                item.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .collect(Collectors.toList()) : new ArrayList<>();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                itemDtos
        );
    }
}