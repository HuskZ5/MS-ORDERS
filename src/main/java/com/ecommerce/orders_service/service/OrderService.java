package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.client.ProductClient;
import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.ItemReturned;
import com.ecommerce.orders_service.dto.OrderItemDto;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.exceptions.*;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderItem;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final CouponService couponService;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, ProductClient productClient, CouponService couponService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.couponService = couponService;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) throw new OrderNotFoundException("No hay ordenes disponibles");
        return orders.stream().map(this::mapToOrderResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getTodayOrders() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        if (orders.isEmpty()) {
            throw new OrderNotFoundException(
                    "No hay órdenes registradas el día de hoy"
            );
        }

        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    /**
     * HU-03: Convierte el carrito activo de un usuario en una orden de compra.
     * Valida la disponibilidad de stock y obtiene el precio vigente desde {@link ProductClient}
     * para cada producto antes de reducir el inventario y persistir la copia histórica en {@link OrderItem}.
     *
     * @param userId Identificador único del usuario dueño del carrito.
     * @return {@link OrderResponse} con los datos de la orden creada.
     */
    @Transactional
    public OrderResponse createOrderFromCart(Long userId) {
        // 1. Busca el carrito activo del usuario
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Carrito no encontrado para el usuario id: " + userId));

        // 2. Valida que no esté vacío
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new CartEmptyException("El carrito del usuario " + userId + " está vacío");
        }

        // 3. Prepara la orden
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        // 4. Consulta ProductClient por cada ítem, valida stock disponible y congela el precio vigente
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalCalculado = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            ProductResponse currentProduct = productClient.getProductById(cartItem.getProductId());

            if (currentProduct.getStock() != null && currentProduct.getStock() < cartItem.getQuantity()) {
                throw new ProductOutOfStockException(
                        "Stock insuficiente para el producto " + cartItem.getProductId()
                );
            }

            BigDecimal currentUnitPrice = currentProduct.getPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(currentUnitPrice);

            BigDecimal subtotal = currentUnitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalCalculado = totalCalculado.add(subtotal);

            orderItems.add(orderItem);
        }

        couponService.validateAppliedCouponsForOrder(
                cart.getId(),
                userId,
                totalCalculado
        );

        BigDecimal totalFinal = couponService.calculateFinalTotal(
                cart.getId(),
                totalCalculado
        );

        order.setItems(orderItems);
        order.setTotal(totalFinal);

        // 5. Descuenta stock en el microservicio de productos tras validar todos los ítems
        for (CartItem cartItem : cart.getItems()) {
            productClient.reduceStock(cartItem.getProductId(), cartItem.getQuantity());
        }

        // 6. Guarda la orden y congela los registros históricos
        Order savedOrder = orderRepository.save(order);

        couponService.registerCouponUsages(
                cart.getId(),
                savedOrder,
                userId
        );

        // 7. Vacía el carrito activo
        cart.getItems().clear();
        cartRepository.save(cart);

        // 8. Retorna el DTO
        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new UnauthorizedUserException(
                    "Usuario no autenticado"
            );
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Pedido no encontrado con id: " + id
                ));

        if (!order.getUserId().equals(authenticatedUserId)) {
            throw new UnauthorizedOrderAccessException(
                    "No tienes autorización para consultar esta orden"
            );
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public CancelOrderResponse cancelOrder(CancelOrderRequest request) {
        Long orderId = request.getOrderId();
        Long userId = request.getUserId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Pedido no encontrado con id: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedOrderAccessException("El usuario con id " + userId + " no tiene autorización para cancelar este pedido");
        }

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("El pedido con id " + orderId + " ya se encuentra cancelado");
        }

        if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("No es posible cancelar el pedido con id " + orderId + " porque se encuentra en estado: " + currentStatus);
        }

        for (OrderItem item : order.getItems()) {
            productClient.restoreStock(
                    item.getProductId(),
                    item.getQuantity()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        List<ItemReturned> itemsReturned = updatedOrder.getItems() != null ?
                updatedOrder.getItems().stream()
                        .map(item -> new ItemReturned(item.getProductId(), item.getQuantity()))
                        .collect(Collectors.toList()) : new ArrayList<>();

        return CancelOrderResponse.builder()
                .orderId(updatedOrder.getId())
                .userId(updatedOrder.getUserId())
                .estatus(updatedOrder.getStatus())
                .refundAmount(updatedOrder.getTotal())
                .itemsReturned(itemsReturned)
                .cancelledAt(updatedOrder.getCancelledAt())
                .build();
    }

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
