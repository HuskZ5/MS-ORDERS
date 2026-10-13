package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * HU-03: Endpoint para crear la orden desde el carrito
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestParam Long userId) {
        OrderResponse response = orderService.createOrderFromCart(userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}