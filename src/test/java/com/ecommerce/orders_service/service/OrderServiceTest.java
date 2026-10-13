package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderItem;
import com.ecommerce.orders_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldGetOrderByIdWhenOrderExists() {

        Order order = new Order(
                1L,
                new BigDecimal("330.00"),
                "CREATED"
        );

        order.setId(1L);
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("60.00"));
        item1.setOrder(order);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProductId(2L);
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("70.00"));
        item2.setOrder(order);

        order.getItems().add(item1);
        order.getItems().add(item2);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(new BigDecimal("330.00"), response.getTotal());
        assertEquals("CREATED", response.getStatus());
        assertEquals(2, response.getItems().size());

        assertEquals(1L, response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("60.00"), response.getItems().get(0).getUnitPrice());

        assertEquals(2L, response.getItems().get(1).getProductId());
        assertEquals(3, response.getItems().get(1).getQuantity());
        assertEquals(new BigDecimal("70.00"), response.getItems().get(1).getUnitPrice());
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals(
                "Pedido no encontrado con id: 999",
                exception.getMessage()
        );
    }
}