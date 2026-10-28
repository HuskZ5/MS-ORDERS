package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.ApiErrorResponse;
import com.ecommerce.orders_service.dto.ApplyCouponRequest;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.dto.UpdateCartItemQuantityRequest;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.service.CartService;
import com.ecommerce.orders_service.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/v1/cart", "/api/cart", "/api/carts"})
public class CartController {

    private final CartService cartService;
    private final CouponService couponService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @Valid @RequestBody AddToCartRequest request) {

        CartResponse response = cartService.addItemToCart(request);

        return ResponseEntity.ok(response);
    }

    // PATCH /api/v1/cart/items/{itemId}
    @Operation(
            summary = "Cambiar la cantidad de un producto en el carrito",
            description = "Actualiza la cantidad de una línea específica del carrito de compras. Valida que el ítem exista, que la cantidad solicitada se encuentre entre 1 y 5 unidades, que exista stock suficiente en el catálogo de productos y que el nuevo subtotal del carrito no supere el límite máximo permitido ($20,000), recalculando los totales en BigDecimal."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cantidad del producto actualizada exitosamente y totales del carrito recalculados",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CartResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cantidad inválida (fuera del rango 1 a 5) o el carrito supera el monto máximo permitido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El ítem especificado no existe en el carrito o el producto no fue encontrado en el catálogo",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Stock insuficiente en el inventario para cubrir la nueva cantidad solicitada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request) {

        CartResponse response = cartService.updateItemQuantity(itemId, request);

        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/cart/items/{itemId}
    @Operation(
            summary = "Retirar un producto del carrito",
            description = "Elimina de forma permanente una línea de producto del carrito activo a partir de su identificador único (itemId) y actualiza el estado persistido del carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Producto retirado exitosamente del carrito (Sin contenido en el cuerpo de respuesta)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El ítem solicitado no fue encontrado en ningún carrito activo",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeProductFromCart(
            @PathVariable Long itemId) {

        cartService.removeItemFromCart(itemId);

        return ResponseEntity.noContent().build();
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
