package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.service.CartService;
import com.ecommerce.orders_service.service.CouponService;
import com.ecommerce.orders_service.dto.ApplyCouponRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final CouponService couponService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(@RequestHeader(value = "X-USER-ID", required = false)
                                                             Long userId,
            @Valid @RequestBody AddToCartRequest request) {

        if (userId == null) {
            throw new UnauthorizedUserException(
                    "Usuario no autenticado");
        }

        request.setUserId(userId);
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

    @PostMapping("/{userId}/coupons")
    public ResponseEntity<CartResponse> applyCoupon(
            @PathVariable Long userId,
            @RequestBody ApplyCouponRequest request) {

        couponService.applyCoupon(userId, request.getCode());

        CartResponse response =
                cartService.getCarByAuthenticatedUser(userId);

        return ResponseEntity.ok(response);
    }

}