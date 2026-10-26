package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.ShippingRequest;
import com.ecommerce.orders_service.dto.ShippingResponse;
import com.ecommerce.orders_service.service.ShipmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @Operation(
            summary = "Actualizar el envío de una orden",
            description = "Permite a un administrador actualizar el estado " +
                    "del envío de una orden pagada."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Envío actualizado correctamente"),
            @ApiResponse(responseCode = "400",
                    description = "Datos de envío inválidos"),
            @ApiResponse(responseCode = "401",
                    description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403",
                    description = "Se requiere el rol ADMIN"),
            @ApiResponse(responseCode = "404",
                    description = "Orden no encontrada"),
            @ApiResponse(responseCode = "409",
                    description = "Estado o transición no permitida")
    })
    @PatchMapping("/{id}/shipping")
    public ResponseEntity<ShippingResponse> updateShipping(
            @PathVariable Long id,
            @RequestBody ShippingRequest request) {

        ShippingResponse response = shipmentService.updateShipping(
                id,
                request
        );

        return ResponseEntity.ok(response);
    }
}
