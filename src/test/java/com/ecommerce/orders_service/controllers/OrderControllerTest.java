package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.ItemReturned;
import com.ecommerce.orders_service.dto.OrderItemDto;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.GlobalExceptionHandler;
import com.ecommerce.orders_service.exceptions.InvalidOrderStateException;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import com.ecommerce.orders_service.exceptions.UnauthorizedOrderAccessException;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias para el controlador web {@link OrderController}.
 * Utiliza MockMvc en modo standalone integrando el {@link GlobalExceptionHandler}.
 */
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

    /**
     * HU-15: Valida la cancelación exitosa de un pedido enviando CancelOrderRequest en el cuerpo JSON.
     * Retorna HTTP 200 OK y estatus "CANCELLED".
     */
    @Test
    void shouldCancelOrderSuccessfully() throws Exception {
        Long orderId = 1L;
        Long userId = 1L;

        CancelOrderResponse response = CancelOrderResponse.builder()
                .orderId(orderId)
                .userId(userId)
                .estatus(OrderStatus.CANCELLED)
                .refundAmount(new BigDecimal("200.00"))
                .itemsReturned(List.of(new ItemReturned(101L, 2)))
                .cancelledAt(LocalDateTime.now())
                .build();

        when(orderService.cancelOrder(any(CancelOrderRequest.class))).thenReturn(response);

        String jsonRequest = "{\"orderId\":1,\"userId\":1}";

        mockMvc.perform(patch("/api/orders/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.estatus").value("CANCELLED"))
                .andExpect(jsonPath("$.refundAmount").value(200.00))
                .andExpect(jsonPath("$.itemsReturned.length()").value(1))
                .andExpect(jsonPath("$.itemsReturned[0].productId").value(101))
                .andExpect(jsonPath("$.itemsReturned[0].quantity").value(2));
    }

    /**
     * HU-15: Valida que si no se proporciona el userId en la solicitud, se retorne HTTP 401 Unauthorized.
     */
    @Test
    void shouldReturnUnauthorizedWhenUserIdIsMissingOnCancel() throws Exception {
        String jsonRequest = "{\"orderId\":1}";

        mockMvc.perform(patch("/api/orders/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario no autenticado"));
    }

    /**
     * HU-15: Valida que si el usuario no tiene permisos para cancelar el pedido,
     * se retorne HTTP 403 Forbidden.
     */
    @Test
    void shouldReturnForbiddenWhenUserIsNotOwnerOnCancel() throws Exception {
        when(orderService.cancelOrder(any(CancelOrderRequest.class)))
                .thenThrow(new UnauthorizedOrderAccessException("El usuario con id 2 no tiene autorización para cancelar este pedido"));

        String jsonRequest = "{\"orderId\":1,\"userId\":2}";

        mockMvc.perform(patch("/api/orders/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("El usuario con id 2 no tiene autorización para cancelar este pedido"));
    }

    /**
     * HU-15: Valida que si el pedido ya está en estado CANCELLED o no permitido,
     * se retorne HTTP 400 Bad Request.
     */
    @Test
    void shouldReturnBadRequestWhenOrderStateIsInvalidOnCancel() throws Exception {
        when(orderService.cancelOrder(any(CancelOrderRequest.class)))
                .thenThrow(new InvalidOrderStateException("El pedido con id 1 ya se encuentra cancelado"));

        String jsonRequest = "{\"orderId\":1,\"userId\":1}";

        mockMvc.perform(patch("/api/orders/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El pedido con id 1 ya se encuentra cancelado"));
    }
}