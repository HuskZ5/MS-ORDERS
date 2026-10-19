package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.client.ProductClient;
import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.exceptions.BusinessOperationException;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartBusinessRulesTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;
    @BeforeEach
    void setup() {
        lenient().when(productClient.getProductById(anyLong()))
                .thenReturn(new ProductResponse("Producto", "Genérico", new BigDecimal("10.00"), 10));

        Cart cart = new Cart();
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>());

        lenient().when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        lenient().when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    }


    @Test
    void testMaxUnitsPerProduct() {
        AddToCartRequest request = new AddToCartRequest(1L, 101L, 6);
        assertThrows(BusinessOperationException.class, () -> cartService.addItemToCart(request));
    }

    @Test
    void testMaxLines() {
        Cart cart = new Cart();
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>());
        for (int i = 1; i <= 10; i++) {
            CartItem item = new CartItem();
            item.setProductId((long) i);
            item.setQuantity(1);
            item.setPrice(new BigDecimal("10.00"));
            item.setCart(cart);
            cart.getItems().add(item);
        }

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        AddToCartRequest extra = new AddToCartRequest(1L, 999L, 1);
        assertThrows(BusinessOperationException.class, () -> cartService.addItemToCart(extra));
    }

    @Test
    void testMaxTotal() {
        when(productClient.getProductById(101L))
                .thenReturn(new ProductResponse("Laptop", "Lenovo", new BigDecimal("200.00"), 10));

        AddToCartRequest request = new AddToCartRequest(1L, 101L, 5); // 5 * 200 = 1000
        assertThrows(BusinessOperationException.class, () -> cartService.addItemToCart(request));
    }
}
