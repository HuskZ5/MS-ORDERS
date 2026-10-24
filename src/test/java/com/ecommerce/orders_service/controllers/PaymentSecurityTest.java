
package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.config.SecurityConfig;
import com.ecommerce.orders_service.exceptions.InvalidPaymentException;
import com.ecommerce.orders_service.service.PaymentService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@Import(SecurityConfig.class)
@DisplayName("PaymentSecurity - Pruebas de seguridad HU-08")
class PaymentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Debe rechazar pagos sin JWT")
    void shouldRejectPaymentWithoutJwt() throws Exception {

        mockMvc.perform(
                post("/api/orders/{id}/payment", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operationId\":\"payment-security-001\"}")
        )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paymentService);
    }


    @Test
    @DisplayName("Debe permitir procesar una solicitud con JWT válido")
    void shouldAllowPaymentWithValidJwt() throws Exception {

        Long orderId = 1L;
        Long userId = 10L;

        Jwt jwt = new Jwt(
                "valid-test-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                Map.of("sub", String.valueOf(userId))
        );

        when(jwtDecoder.decode("valid-test-token"))
                .thenReturn(jwt);

        when(paymentService.processPayment(
                eq(orderId),
                eq(userId),
                any()
        )).thenThrow(new InvalidPaymentException("Prueba de acceso autorizado"));

        mockMvc.perform(
                        post("/api/orders/{id}/payment", orderId)
                                .header("Authorization", "Bearer valid-test-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"operationId\":\"payment-security-002\"}")
                )
                .andExpect(status().isBadRequest());
    }

}
