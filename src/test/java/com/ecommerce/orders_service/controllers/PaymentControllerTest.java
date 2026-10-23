
package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.config.WebControllerTest;
import com.ecommerce.orders_service.dto.PaymentRequest;
import com.ecommerce.orders_service.dto.PaymentResponse;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.model.PaymentStatus;
import com.ecommerce.orders_service.service.PaymentService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebControllerTest(controllers = PaymentController.class)
@DisplayName("PaymentController - Pruebas HU-08")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Debe responder 200 cuando el pago es exitoso")
    void shouldReturn200WhenPaymentIsSuccessful() throws Exception {

        Long orderId = 1L;
        Long userId = 10L;

        PaymentRequest request = new PaymentRequest();
        request.setOperationId("payment-controller-001");

        PaymentResponse response = new PaymentResponse(
                1L,
                orderId,
                request.getOperationId(),
                new BigDecimal("250.00"),
                PaymentStatus.SUCCESS,
                OrderStatus.PAID,
                LocalDateTime.now(),
                "Pago simulado procesado correctamente"
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(userId),
                        null,
                        Collections.emptyList()
                );

        when(paymentService.processPayment(
                org.mockito.ArgumentMatchers.eq(orderId),
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any(PaymentRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                post("/api/orders/{id}/payment", orderId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.orderStatus").value("PAID"))
                .andExpect(jsonPath("$.operationId")
                        .value("payment-controller-001"));
    }


    @Test
    @DisplayName("Debe responder 400 cuando operationId está vacío")
    void shouldReturn400WhenOperationIdIsBlank() throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "10",
                        null,
                        Collections.emptyList()
                );

        mockMvc.perform(
                        post("/api/orders/{id}/payment", 1L)
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"operationId\":\"\"}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paymentService);
    }


    @Test
    @DisplayName("Debe responder 401 cuando el usuario no está autenticado")
    void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {

        mockMvc.perform(
                        post("/api/orders/{id}/payment", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"operationId\":\"payment-001\"}")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("Usuario no autenticado"));

        verifyNoInteractions(paymentService);
    }


    @Test
    @DisplayName("Debe responder 401 cuando el identificador del usuario no es numérico")
    void shouldReturn401WhenAuthenticatedUserIdIsInvalid() throws Exception {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "usuario-invalido",
                        null,
                        Collections.emptyList()
                );

        mockMvc.perform(
                        post("/api/orders/{id}/payment", 1L)
                                .principal(authentication)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"operationId\":\"payment-002\"}")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error")
                        .value("No se pudo identificar al usuario autenticado"));

        verifyNoInteractions(paymentService);
    }



}
