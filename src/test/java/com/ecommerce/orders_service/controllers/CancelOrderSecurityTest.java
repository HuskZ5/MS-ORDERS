package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.config.SecurityConfig;
import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.exceptions.GlobalExceptionHandler;
import com.ecommerce.orders_service.exceptions.UnauthorizedOrderAccessException;
import com.ecommerce.orders_service.service.OrderService;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.math.BigDecimal;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = true)
@DisplayName("HU-07 - Seguridad en cancelación de órdenes")
@ActiveProfiles("local")
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class CancelOrderSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("Retorna 401 cuando se intenta cancelar una orden sin JWT")
    void shouldReturn401WhenJwtIsMissing() throws Exception {

        mockMvc.perform(
                        patch("/api/orders/{id}/cancel", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\":1}")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("Retorna 401 cuando el JWT es inválido")
    void shouldReturn401WhenJwtIsInvalid() throws Exception {

        mockMvc.perform(
                        patch("/api/orders/{id}/cancel", 1L)
                                .header("Authorization", "Bearer token-invalido")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\":1}")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("Retorna 200 cuando el usuario autenticado cancela su orden")
    void shouldReturn200WhenAuthenticatedUserCancelsOrder() throws Exception {

        CancelOrderResponse response = CancelOrderResponse.builder()
                .orderId(1L)
                .userId(10L)
                .estatus(OrderStatus.CANCELLED)
                .refundAmount(new BigDecimal("200.00"))
                .build();

        when(orderService.cancelOrder(any(CancelOrderRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/orders/{id}/cancel", 1L)
                                .with(jwt().jwt(token -> token.subject("10")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\":1,\"userId\":99}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.estatus").value("CANCELLED"));

        verify(orderService).cancelOrder(
                org.mockito.ArgumentMatchers.argThat(request ->
                        Long.valueOf(1L).equals(request.getOrderId())
                                && Long.valueOf(10L).equals(request.getUserId())
                )
        );
    }

    @Test
    @DisplayName("Retorna 403 cuando el usuario intenta cancelar una orden ajena")
    void shouldReturn403WhenUserDoesNotOwnOrder() throws Exception {

        when(orderService.cancelOrder(any(CancelOrderRequest.class)))
                .thenThrow(new UnauthorizedOrderAccessException(
                        "El usuario con id 20 no tiene autorización para cancelar este pedido"
                ));

        mockMvc.perform(
                        patch("/api/orders/{id}/cancel", 1L)
                                .with(jwt().jwt(token -> token.subject("20")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\":1,\"userId\":10}")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(
                        "El usuario con id 20 no tiene autorización para cancelar este pedido"
                ));

        verify(orderService).cancelOrder(
                org.mockito.ArgumentMatchers.argThat(request ->
                        Long.valueOf(1L).equals(request.getOrderId())
                                && Long.valueOf(20L).equals(request.getUserId())
                )
        );
    }

    @Test
    @DisplayName("Retorna 400 cuando el ID de la URL no coincide con el JSON")
    void shouldReturn400WhenOrderIdsDoNotMatch() throws Exception {

        mockMvc.perform(
                        patch("/api/orders/{id}/cancel", 1L)
                                .with(jwt().jwt(token -> token.subject("10")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\":2}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("Retorna 400 cuando no se proporciona el ID de la orden")
    void shouldReturn400WhenOrderIdIsMissing() throws Exception {

        mockMvc.perform(
                        patch("/api/orders/cancel")
                                .with(jwt().jwt(token -> token.subject("10")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "El ID de la orden es obligatorio"
                ));

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("Retorna 401 cuando el identificador del JWT no es numérico")
    void shouldReturn401WhenJwtSubjectIsNotNumeric() throws Exception {

        mockMvc.perform(
                        patch("/api/orders/{id}/cancel", 1L)
                                .with(jwt().jwt(token -> token.subject("usuario-invalido")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"orderId\":1}")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(
                        "Identidad de usuario inválida"
                ));

        verifyNoInteractions(orderService);
    }

}