package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.OrderItemDto;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.CartEmptyException;
import com.ecommerce.orders_service.exceptions.CartNotFoundException;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderItem;
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
        order.setStatus("CREATED");
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
     * Obtener los detalles de una orden por su ID (HU-14 / GET Order)
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido no encontrado con id: " + id));

        return mapToOrderResponse(order);
    }

    /**
     * Mapear la entidad Order a OrderResponse DTO
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
                order.getStatus(),
                order.getCreatedAt(),
                itemDtos
        );
    }
}