package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.OrderNotFoundException;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderItem;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.repository.OrderRepository;
import com.ecommerce.orders_service.client.ProductClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;



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

    @Test
    void shouldGetOrderByIdWhenOrderExists() {

        Order order = new Order(
                1L,
                new BigDecimal("330.00"),
                "CREATED"
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

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

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

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {

        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(999L)
        );

        assertEquals(
                "Pedido no encontrado con id: 999",
                exception.getMessage()
        );
    }

    @Test
    void shouldReduceStockWhenCreatingOrder() {

        // ARRANGE: Preparamos un carrito simulado
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

        cart.setItems(new java.util.ArrayList<>(
                List.of(item1, item2)
        ));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        OrderResponse response =
                orderService.createOrderFromCart(userId);

        verify(productClient).reduceStock(101L, 3);
        verify(productClient).reduceStock(102L, 2);

        assertEquals(
                new BigDecimal("700.00"),
                response.getTotal()
        );
    }


    @Test
    void shouldNotCreateOrderWhenStockIsInsufficient() {

        // ARRANGE: Preparamos el carrito
        Long userId = 5L;

        Cart cart = new Cart();
        cart.setUserId(userId);

        CartItem item = new CartItem();
        item.setProductId(101L);
        item.setQuantity(5);
        item.setPrice(new BigDecimal("100.00"));
        item.setCart(cart);

        cart.setItems(new java.util.ArrayList<>(
                List.of(item)
        ));

        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(cart));

        doThrow(new ProductOutOfStockException(
                "Stock insuficiente para el producto 101"
        )).when(productClient).reduceStock(101L, 5);

        ProductOutOfStockException exception = assertThrows(
                ProductOutOfStockException.class,
                () -> orderService.createOrderFromCart(userId)
        );

        assertEquals(
                "Stock insuficiente para el producto 101",
                exception.getMessage()
        );

        verify(orderRepository, never()).save(any(Order.class));

        verify(cartRepository, never()).save(any(Cart.class));

        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }



}