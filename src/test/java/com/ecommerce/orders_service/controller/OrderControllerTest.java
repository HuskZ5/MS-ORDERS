package com.ecommerce.orders_service.controller;

import com.ecommerce.orders_service.dto.OrderItemDto;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.service.OrderService;
import com.ecommerce.orders_service.exceptions.GlobalExceptionHandler;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        OrderController orderController = new OrderController(orderService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnOrderWhenOrderExists() throws Exception {

        OrderItemDto item1 = new OrderItemDto(
                1L,
                1L,
                2,
                new BigDecimal("60.00")
        );

        OrderItemDto item2 = new OrderItemDto(
                2L,
                2L,
                3,
                new BigDecimal("70.00")
        );

        OrderResponse response = new OrderResponse(
                1L,
                1L,
                new BigDecimal("330.00"),
                "CREATED",
                LocalDateTime.of(2026, 10, 8, 20, 51, 3),
                List.of(item1, item2)
        );

        when(orderService.getOrderById(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.total").value(330.00))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(60.00))
                .andExpect(jsonPath("$.items[1].productId").value(2))
                .andExpect(jsonPath("$.items[1].quantity").value(3))
                .andExpect(jsonPath("$.items[1].unitPrice").value(70.00));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {

        when(orderService.getOrderById(999L))
                .thenThrow(new OrderNotFoundException(
                        "Pedido no encontrado con id: 999"
                ));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Pedido no encontrado con id: 999"));
    }
}