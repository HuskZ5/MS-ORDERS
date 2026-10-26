package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.config.SecurityConfig;
import com.ecommerce.orders_service.dto.ShippingResponse;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.model.ShipmentStatus;
import com.ecommerce.orders_service.service.ShipmentService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

@WebMvcTest(controllers = ShipmentController.class)
@Import(SecurityConfig.class)
@DisplayName("ShipmentSecurity - Pruebas de seguridad HU-09")
class ShipmentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentService shipmentService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Debe rechazar actualización de envío sin JWT")
    void shouldRejectShippingWithoutJwt() throws Exception {

        mockMvc.perform(
                patch("/api/orders/{id}/shipping", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}")
        )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(shipmentService);
    }

    @Test
    @DisplayName("Debe rechazar actualización de envío con rol USER")
    void shouldRejectShippingWithUserRole() throws Exception {

        mockMvc.perform(
                patch("/api/orders/{id}/shipping", 1L)
                        .with(jwt().jwt(token -> token
                                .subject("10")
                                .claim("roles", List.of("USER"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}")
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(shipmentService);
    }

    @Test
    @DisplayName("Debe permitir actualización de envío con rol ADMIN")
    void shouldAllowShippingWithAdminRole() throws Exception {

        ShippingResponse response = new ShippingResponse(
                1L,
                1L,
                ShipmentStatus.SHIPPED,
                OrderStatus.SHIPPED,
                "RALLY-001",
                null,
                null,
                "Envío actualizado correctamente"
        );

        when(shipmentService.updateShipping(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(
                patch("/api/orders/{id}/shipping", 1L)
                        .with(jwt().jwt(token -> token
                                .subject("10")
                                .claim("roles", List.of("ADMIN")))
                                .authorities(
                                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")
                                ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\",\"trackingNumber\":\"RALLY-001\"}")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.orderStatus").value("SHIPPED"));

        verify(shipmentService).updateShipping(eq(1L), any());
    }
}
