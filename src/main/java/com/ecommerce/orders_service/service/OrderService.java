package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.ItemReturned;
import com.ecommerce.orders_service.dto.OrderItemDto;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.CartEmptyException;
import com.ecommerce.orders_service.exceptions.CartNotFoundException;
import com.ecommerce.orders_service.exceptions.InvalidOrderStateException;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import com.ecommerce.orders_service.exceptions.UnauthorizedOrderAccessException;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderItem;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.repository.OrderRepository;
import com.ecommerce.orders_service.client.ProductClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de negocio para gestionar el ciclo de vida de los pedidos.
 * Proporciona operaciones de creación, consulta por ID y cancelación de pedidos.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    /**
     * HU-03: Convierte el carrito activo de un usuario en una orden de compra.
     * Realiza la reducción de stock mediante el cliente de productos y vacía el carrito.
     *
     * @param userId Identificador único del usuario dueño del carrito.
     * @return {@link OrderResponse} con los datos de la orden creada.
     */
    @Transactional
    public OrderResponse createOrderFromCart(Long userId) {
        // 1. Busca el carrito activo del usuario
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrito no encontrado para el usuario id: " + userId));

        // 2. Valida que no este vacio
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartEmptyException("El carrito del usuario " + userId + " está vacío");
        }

        // 3. Prepara la orden
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        // 4. Stop precios y calcula total acumulado
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalCalculado = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPrice());

            BigDecimal subtotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalCalculado = totalCalculado.add(subtotal);

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotal(totalCalculado);
        for (CartItem cartItem : cart.getItems()) {
            productClient.reduceStock(cartItem.getProductId(), cartItem.getQuantity());
        }

        // 5. Guarda la orden y congela los registros
        Order savedOrder = orderRepository.save(order);

        // 6. Vacia el carrito activo
        cart.getItems().clear();
        cartRepository.save(cart);

        // 7. Retorna el dto
        return mapToOrderResponse(savedOrder);
    }

    /**
     * Obtener los detalles de una orden por su ID (HU-14 / GET Order).
     *
     * @param id Identificador único de la orden.
     * @return {@link OrderResponse} con el detalle de la orden.
     * @throws OrderNotFoundException si la orden no existe.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido no encontrado con id: " + id));

        return mapToOrderResponse(order);
    }

    /**
     * HU-15: Cancela un pedido existente previa validación de existencia, autorización y estado permitido.
     *
     * @param request DTO {@link CancelOrderRequest} con los datos del pedido y usuario solicitante.
     * @return {@link CancelOrderResponse} con el resultado detallado de la cancelación y monto reembolsado.
     * @throws OrderNotFoundException si el pedido no existe.
     * @throws UnauthorizedOrderAccessException si el usuario no tiene permisos para cancelar este pedido.
     * @throws InvalidOrderStateException si el pedido no se encuentra en un estado cancelable.
     */
    @Transactional
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        Long orderId = request.getOrderId();
        Long userId = request.getUserId();

        // 1. Verificar existencia del pedido
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido no encontrado con id: " + orderId));

        // 2. Validar autorización de usuario (pertenencia)
        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("El usuario con id " + userId + " no tiene autorización para cancelar este pedido");
        }

        // 3. Validar estado permitido
        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("El pedido con id " + orderId + " ya se encuentra cancelado");
        }

        if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("No es posible cancelar el pedido con id " + orderId + " porque se encuentra en estado: " + currentStatus);
        }

        // 4. Cambiar estado a CANCELLED y persistir
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        // 5. Mapear items devueltos
        List<ItemReturned> itemsReturned = updatedOrder.getItems() != null ?
                updatedOrder.getItems().stream()
                        .map(item -> new ItemReturned(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList()) : new ArrayList<>();

        // 6. Retornar DTO de cancelación
        return CancelOrderResponse.builder()
                .orderId(updatedOrder.getId())
                .userId(updatedOrder.getUserId())
                .estatus(updatedOrder.getStatus())
                .refundAmount(updatedOrder.getTotal())
                .itemsReturned(itemsReturned)
                .cancelledAt(updatedOrder.getCancelledAt())
                .build();
    }

    /**
     * Mapear la entidad Order a OrderResponse DTO.
     *
     * @param order Entidad {@link Order} a transformar.
     * @return Instancia de {@link OrderResponse} con los datos estructurados.
     */
    public OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems() != null ?
                order.getItems().stream()
                        .map(item -> new OrderItemDto(
                                item.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .collect(Collectors.toList()) : new ArrayList<>();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getTotal(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getCreatedAt(),
                itemDtos
        );
    }
}