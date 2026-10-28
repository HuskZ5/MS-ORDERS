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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/v1/cart", "/api/cart", "/api/carts"})
@Tag(name = "Carrito de Compras", description = "Endpoints para la administración de ítems del carrito de compras")
public class CartController {

    private final CartService cartService;
    private final CouponService couponService;

    @Operation(
            summary = "Agregar un producto al carrito",
            description = "Agrega un producto al carrito activo del usuario o acumula su cantidad validando límites de negocio."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto agregado correctamente al carrito"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o violación de regla de negocio",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado en el catálogo",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @Valid @RequestBody AddToCartRequest request) {

        CartResponse response = cartService.addItemToCart(request);

        return ResponseEntity.ok(response);
    }

    // PATCH /api/v1/cart/items/{itemId}
    // This endpoint updates the quantity of a specific item in the cart, ensuring that the new quantity is
    // within allowed limits and that there is sufficient stock available.
    // It returns the updated cart response if successful, or an error response if any validation fails.
    @Operation(
            summary = "Actualizar la cantidad de un producto en el carrito",
            description = "Modifica la cantidad de una línea específica del carrito validando disponibilidad de inventario, límite máximo por producto (5) y tope monetario en el backend."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada exitosamente y totales recalculados",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad fuera de rango permitido o límite del carrito excedido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ítem del carrito o producto no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente para la cantidad solicitada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request) {

        CartResponse response = cartService.updateItemQuantity(itemId, request);

        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/cart/items/{itemId}
    // This endpoint removes a specific item from the cart based on its unique identifier.
    // It returns a 204 No Content response if the item is successfully removed,
    // or a 404 Not Found response if the item does not exist in any active cart.
    @Operation(
            summary = "Retirar un producto del carrito",
            description = "Elimina permanentemente una línea de producto del carrito de compras mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ítem eliminado exitosamente del carrito (Sin contenido)"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado en ningún carrito activo",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
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
