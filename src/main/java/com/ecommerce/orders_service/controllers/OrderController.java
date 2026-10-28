package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.ErrorResponse;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar pedidos en el microservicio Orders Service.
 * Expone endpoints para la creación, consulta y cancelación de pedidos.
 */
@RestController
@RequestMapping({"/api/v1/orders", "/api/orders"})
@SecurityScheme(
        name = "bearerAuth",
        type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Autenticación mediante token JWT"
)
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/today")
    public ResponseEntity<List<OrderResponse>> getTodayOrders() {
        return ResponseEntity.ok(orderService.getTodayOrders());
    }

    /**
     * HU-14: Endpoint para obtener los detalles de un pedido por su identificador único.
     *
     * @param id Identificador único del pedido a consultar.
     * @return {@link ResponseEntity} con el {@link OrderResponse} correspondiente.
     */
    @Operation(
            summary = "Consultar una orden por ID",
            description = "Permite consultar una orden únicamente si pertenece al usuario autenticado mediante JWT."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden encontrada y pertenece al usuario autenticado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente, inválido o expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene autorización para consultar esta orden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La orden solicitada no existe"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedUserException("Usuario no autenticado");
        }

        Long authenticatedUserId;

        try {
            authenticatedUserId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new UnauthorizedUserException(
                    "No se pudo identificar al usuario autenticado"
            );
        }

        OrderResponse response = orderService.getOrderById(
                id,
                authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * HU-03: Endpoint para crear una orden a partir de los ítems del carrito activo de un usuario.
     *
     * @param userId Identificador único del usuario dueño del carrito.
     * @return {@link ResponseEntity} con el {@link OrderResponse} creado y estado HTTP 201 Created.
     */
    @Operation(
            summary = "Crear una orden de compra desde el carrito activo",
            description = "Valida la existencia del carrito, consulta el catálogo de productos en tiempo real para verificar stock disponible y congelar precios vigentes en BigDecimal, aplica descuentos de cupones, deduce el inventario y genera el registro histórico de la orden."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Orden creada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El carrito se encuentra vacío o los cupones aplicados son inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El carrito del usuario o alguno de los productos no existe",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Stock insuficiente en el inventario para cubrir la cantidad solicitada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestParam Long userId) {
        OrderResponse response = orderService.createOrderFromCart(userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Cancelar una orden",
            description = "Permite cancelar una orden que pertenece al usuario " +
                    "autenticado mediante JWT. Valida el estado de la orden y " +
                    "solicita la restauración del inventario de sus productos."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orden cancelada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud de cancelación inválida",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    value = "{\"error\":\"El ID de la orden es obligatorio\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT ausente, inválido o expirado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    value = "{\"error\":\"Usuario no autenticado\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no tiene autorización para cancelar esta orden",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    value = "{\"error\":\"No tienes permiso para cancelar esta orden\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La orden solicitada no existe",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    value = "{\"error\":\"Orden no encontrada\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La orden ya fue cancelada o se encuentra en un estado que impide su cancelación",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "object"),
                            examples = @ExampleObject(
                                    value = "{\"error\":\"La orden no puede cancelarse en su estado actual\"}"
                            )
                    )
            )
    })
    @PatchMapping(value = {"/cancel", "/{id}/cancel"})
    public ResponseEntity<CancelOrderResponse> cancelOrder(
            @PathVariable(value = "id", required = false) Long id,
            @RequestBody CancelOrderRequest request,
            Authentication authentication) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "La solicitud de cancelación es obligatoria"
            );
        }

        if (id != null) {
            if (request.getOrderId() != null
                    && !id.equals(request.getOrderId())) {
                throw new IllegalArgumentException(
                        "El ID de la URL no coincide con el ID de la solicitud"
                );
            }

            request.setOrderId(id);
        }

        if (request.getOrderId() == null) {
            throw new IllegalArgumentException(
                    "El ID de la orden es obligatorio"
            );
        }

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedUserException("Usuario no autenticado");
        }

        Long authenticatedUserId;

        try {
            authenticatedUserId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            throw new UnauthorizedUserException("Identidad de usuario inválida");
        }

        request.setUserId(authenticatedUserId);

        CancelOrderResponse response = orderService.cancelOrder(request);
        return ResponseEntity.ok(response);
    }

}
