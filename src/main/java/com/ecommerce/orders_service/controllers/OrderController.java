package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.service.OrderService;
import com.ecommerce.orders_service.model.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.time.LocalDate;

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


    @GetMapping("/today")
    public ResponseEntity<List<OrderResponse>> getTodayOrders() {
        return ResponseEntity.ok(orderService.getTodayOrders());
    }

    @GetMapping("/history")
    public ResponseEntity<Page<OrderResponse>> getOrderHistory(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate FI,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate FF,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        throw new UnauthorizedUserException(
                "El historial requiere autenticación verificada"
        );
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
     * HU-03: Endpoint para crear una orden a partir de los ítems del carrito activo de un usuario.
     *
     * @param userId Identificador único del usuario dueño del carrito.
     * @return {@link ResponseEntity} con el {@link OrderResponse} creado y estado HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestParam Long userId) {
        OrderResponse response = orderService.createOrderFromCart(userId);
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