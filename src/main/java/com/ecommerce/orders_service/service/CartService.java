package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartItemDto;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final OrderRepository orderRepository;
    // Parte de Miguel ;)

    public CartService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * HU12 - Obtiene el carrito activo de un usuario por su userId
     */
    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {
        // Muestra de respuesta estructurada para conectar con los controllers
        List<CartItemDto> items = new ArrayList<>();
        return new CartResponse(1L, userId, items, BigDecimal.ZERO);
    }

    /**
     * HU11 - Agrega o actualiza la cantidad de un producto en el carrito
     */
    @Transactional
    public CartResponse addItemToCart(AddToCartRequest request) {
        // Calcular el subtotal de la entrada
        BigDecimal itemSubtotal = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        CartItemDto newItem = new CartItemDto(
                1L,
                request.getProductId(),
                request.getQuantity(),
                request.getPrice()
        );

        List<CartItemDto> items = List.of(newItem);

        return new CartResponse(
                1L,
                request.getUserId(),
                items,
                itemSubtotal
        );
    }

    /**
     * Metodo utilitario para mapear la entidad Cart a CartResponse DTO
     */
    public CartResponse mapToCartResponse(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems() != null ?
                cart.getItems().stream()
                        .map(item -> new CartItemDto(
                                item.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .collect(Collectors.toList()) : new ArrayList<>();

        BigDecimal total = itemDtos.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUserId(), itemDtos, total);
    }
}