package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestBody AddToCartRequest request) {

        CartResponse response = cartService.addItemToCart(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId) {

        CartResponse response =
                cartService.getCarByAuthenticatedUser(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CartResponse> getCartForAuthenticatedUser(
            @RequestHeader(value = "X-USER-ID", required = false)
            Long userId) {

        if (userId == null) {
            throw new UnauthorizedUserException(
                    "Usuario no autenticado");
        }

        CartResponse response =
                cartService.getCarByAuthenticatedUser(userId);

        return ResponseEntity.ok(response);
    }
}