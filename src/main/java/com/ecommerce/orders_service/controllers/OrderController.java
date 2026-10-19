package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar pedidos en el microservicio Orders Service.
 * Expone endpoints para la creación, consulta y cancelación de pedidos.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll(){
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * HU-14: Endpoint para obtener los detalles de un pedido por su identificador único.
     *
     * @param id Identificador único del pedido a consultar.
     * @return {@link ResponseEntity} con el {@link OrderResponse} correspondiente.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * HU-03: Endpoint para crear una orden a partir de los ítems del carrito activo de un usuario con Idempotencia.
     * @param userId Identificador único del usuario dueño del carrito.
     * @return {@link ResponseEntity} con el {@link OrderResponse} creado y estado HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestParam Long userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        OrderResponse response = orderService.createOrder(userId, idempotencyKey);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * HU-15: Endpoint para cancelar un pedido existente.
     * <p>
     * Recibe los datos de cancelación mediante el cuerpo de la petición (JSON) con {@link CancelOrderRequest}.
     *
     * @param id Identificador del pedido opcionalmente proveniente de la ruta.
     * @param request DTO {@link CancelOrderRequest} con orderId y userId.
     * @return {@link ResponseEntity} con el {@link CancelOrderResponse} y código HTTP 200 OK.
     * @throws UnauthorizedUserException si el userId no está presente.
     */
    @PatchMapping(value = {"/cancel", "/{id}/cancel"})
    public ResponseEntity<CancelOrderResponse> cancelOrder(
            @PathVariable(value = "id", required = false) Long id,
            @RequestBody CancelOrderRequest request) {

        if (id != null && request.getOrderId() == null) {
            request.setOrderId(id);
        }

        if (request == null || request.getUserId() == null) {
            throw new UnauthorizedUserException("Usuario no autenticado");
        }

        CancelOrderResponse response = orderService.cancelOrder(request);
        return ResponseEntity.ok(response);
    }
}