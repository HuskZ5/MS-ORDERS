
package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.PaymentRequest;
import com.ecommerce.orders_service.dto.PaymentResponse;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Procesar pago de una orden",
            description = "Simula el pago de una orden perteneciente al usuario autenticado."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago procesado o reintento reconocido"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de pago inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "La orden pertenece a otro usuario"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Orden no encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La orden no permite realizar el pago"
            )
    })
    @PostMapping("/{id}/payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedUserException(
                    "Usuario no autenticado"
            );
        }

        Long authenticatedUserId;

        try {
            authenticatedUserId = Long.valueOf(
                    authentication.getName()
            );
        } catch (NumberFormatException ex) {
            throw new UnauthorizedUserException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        PaymentResponse response = paymentService.processPayment(
                id,
                authenticatedUserId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
