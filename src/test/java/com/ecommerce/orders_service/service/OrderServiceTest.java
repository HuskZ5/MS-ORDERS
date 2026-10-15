package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.client.ProductClient;
import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.InvalidOrderStateException;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import com.ecommerce.orders_service.exceptions.UnauthorizedOrderAccessException;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderItem;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la capa de servicio {@link OrderService}.
 * <p>
 * Valida la lógica de negocio para la consulta, creación y cancelación de pedidos,
 * integrando el enum {@link OrderStatus} y los nuevos DTOs {@link CancelOrderRequest} y {@link CancelOrderResponse}.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    /**
     * Valida la consulta exitosa de un pedido por su identificador único cuando el registro existe.
     */
    @Test
    void shouldGetOrderByIdWhenOrderExists() {
        // Arrange
        Order order = new Order(
                1L,
                new BigDecimal("330.00"),
                OrderStatus.CREATED
        );
        order.setId(1L);

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setProductId(1L);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("60.00"));
        item1.setOrder(order);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setProductId(2L);
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("70.00"));
        item2.setOrder(order);

        order.getItems().add(item1);
        order.getItems().add(item2);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        OrderResponse response = orderService.getOrderById(1L);

        // Assert
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(new BigDecimal("330.00"), response.getTotal());
        assertEquals("CREATED", response.getStatus());
        assertEquals(2, response.getItems().size());
        assertEquals(1L, response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("60.00"), response.getItems().get(0).getUnitPrice());
        assertEquals(2L, response.getItems().get(1).getProductId());
        assertEquals(3, response.getItems().get(1).getQuantity());
        assertEquals(new BigDecimal("70.00"), response.getItems().get(1).getUnitPrice());
    }

    /**
     * Valida que se lance {@link OrderNotFoundException} al consultar un pedido inexistente.
     */
    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals("Pedido no encontrado con id: 999", exception.getMessage());
    }

    /**
     * Valida que al crear una orden desde el carrito, se descuente el inventario de cada producto.
     */
    @Test
    void shouldReduceStockWhenCreatingOrder() {
        // Arrange
        Long userId = 5L;

        Cart cart = new Cart();
        cart.setUserId(userId);

        CartItem item1 = new CartItem();
        item1.setProductId(101L);
        item1.setQuantity(3);
        item1.setPrice(new BigDecimal("100.00"));
        item1.setCart(cart);

        CartItem item2 = new CartItem();
        item2.setProductId(102L);
        item2.setQuantity(2);
        item2.setPrice(new BigDecimal("200.00"));
        item2.setCart(cart);

        cart.setItems(new ArrayList<>(List.of(item1, item2)));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderResponse response = orderService.createOrderFromCart(userId);

        // Assert
        verify(productClient).reduceStock(101L, 3);
        verify(productClient).reduceStock(102L, 2);
        assertEquals(new BigDecimal("700.00"), response.getTotal());
    }

    /**
     * Valida que no se persista la orden si el stock de algún producto es insuficiente.
     */
    @Test
    void shouldNotCreateOrderWhenStockIsInsufficient() {
        // Arrange
        Long userId = 5L;

        Cart cart = new Cart();
        cart.setUserId(userId);

        CartItem item = new CartItem();
        item.setProductId(101L);
        item.setQuantity(5);
        item.setPrice(new BigDecimal("100.00"));
        item.setCart(cart);

        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        doThrow(new ProductOutOfStockException("Stock insuficiente para el producto 101"))
                .when(productClient).reduceStock(101L, 5);

        // Act & Assert
        ProductOutOfStockException exception = assertThrows(
                ProductOutOfStockException.class,
                () -> orderService.createOrderFromCart(userId)
        );

        assertEquals("Stock insuficiente para el producto 101", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).save(any(Cart.class));
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    /**
     * HU-15: Valida la cancelación exitosa de un pedido cuando el usuario es dueño y el estado es CREATED.
     * Retorna CancelOrderResponse con estatus CANCELLED, items devueltos y refundAmount.
     */
    @Test
    void shouldCancelOrderSuccessfullyWhenStatusIsCreatedAndUserIsOwner() {
        // Arrange
        Long orderId = 1L;
        Long userId = 1L;

        Order order = new Order(userId, new BigDecimal("250.00"), OrderStatus.CREATED);
        order.setId(orderId);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(10L);
        orderItem.setProductId(101L);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("125.00"));
        orderItem.setOrder(order);
        order.setItems(new ArrayList<>(List.of(orderItem)));

        CancelOrderRequest request = new CancelOrderRequest(orderId, userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CancelOrderResponse response = orderService.cancelOrder(request);

        // Assert
        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(userId, response.getUserId());
        assertEquals(OrderStatus.CANCELLED, response.getEstatus());
        assertEquals(new BigDecimal("250.00"), response.getRefundAmount());
        assertNotNull(response.getCancelledAt());
        assertEquals(1, response.getItemsReturned().size());
        assertEquals(101L, response.getItemsReturned().get(0).getProductId());
        assertEquals(2, response.getItemsReturned().get(0).getQuantity());

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
        verify(productClient, times(1)).restoreStock(101L, 2);

    }

    /**
     * HU-15: Valida que se lance {@link OrderNotFoundException} si el pedido a cancelar no existe.
     */
    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExistOnCancel() {
        Long orderId = 999L;
        Long userId = 1L;
        CancelOrderRequest request = new CancelOrderRequest(orderId, userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(request)
        );

        assertEquals("Pedido no encontrado con id: 999", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(productClient, never()).restoreStock(anyLong(), anyInt());

    }

    /**
     * HU-15: Valida que se lance {@link UnauthorizedOrderAccessException} si el usuario que solicita
     * la cancelación no es el dueño del pedido.
     */
    @Test
    void shouldThrowUnauthorizedOrderAccessExceptionWhenUserIsNotOwnerOnCancel() {
        Long orderId = 1L;
        Long ownerUserId = 1L;
        Long intruderUserId = 2L;

        Order order = new Order(ownerUserId, new BigDecimal("100.00"), OrderStatus.CREATED);
        order.setId(orderId);

        CancelOrderRequest request = new CancelOrderRequest(orderId, intruderUserId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        UnauthorizedOrderAccessException exception = assertThrows(
                UnauthorizedOrderAccessException.class,
                () -> orderService.cancelOrder(request)
        );

        assertEquals("El usuario con id 2 no tiene autorización para cancelar este pedido", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(productClient, never()).restoreStock(anyLong(), anyInt());
    }

    /**
     * HU-15: Valida que se lance {@link InvalidOrderStateException} si se intenta cancelar un pedido
     * que ya se encuentra en estado CANCELLED.
     */
    @Test
    void shouldThrowInvalidOrderStateExceptionWhenOrderIsAlreadyCancelled() {
        Long orderId = 1L;
        Long userId = 1L;

        Order order = new Order(userId, new BigDecimal("100.00"), OrderStatus.CANCELLED);
        order.setId(orderId);

        CancelOrderRequest request = new CancelOrderRequest(orderId, userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderStateException exception = assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.cancelOrder(request)
        );

        assertEquals("El pedido con id 1 ya se encuentra cancelado", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(productClient, never()).restoreStock(anyLong(), anyInt());
    }

    /**
     * HU-15: Valida que se lance {@link InvalidOrderStateException} si el pedido se encuentra
     * en un estado posterior no cancelable (ej. SHIPPED o DELIVERED).
     */
    @Test
    void shouldThrowInvalidOrderStateExceptionWhenOrderIsShippedOrDelivered() {
        Long orderId = 1L;
        Long userId = 1L;

        Order order = new Order(userId, new BigDecimal("100.00"), OrderStatus.SHIPPED);
        order.setId(orderId);

        CancelOrderRequest request = new CancelOrderRequest(orderId, userId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderStateException exception = assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.cancelOrder(request)
        );

        assertEquals("No es posible cancelar el pedido con id 1 porque se encuentra en estado: SHIPPED", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verify(productClient, never()).restoreStock(anyLong(), anyInt());
    }


    @Test
    void shouldNotCancelOrderWhenStockRestorationFails() {

        Long orderId = 1L;
        Long userId = 1L;

        Order order = new Order(
                userId,
                new BigDecimal("250.00"),
                OrderStatus.CREATED
        );
        order.setId(orderId);

        OrderItem item = new OrderItem();
        item.setProductId(101L);
        item.setQuantity(2);
        item.setOrder(order);

        order.setItems(new ArrayList<>(List.of(item)));

        CancelOrderRequest request =
                new CancelOrderRequest(orderId, userId);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        doThrow(new RuntimeException("Error al restaurar stock"))
                .when(productClient)
                .restoreStock(101L, 2);

        assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(request)
        );

        assertEquals(OrderStatus.CREATED, order.getStatus());

        verify(orderRepository, never())
                .save(any(Order.class));
    }

}