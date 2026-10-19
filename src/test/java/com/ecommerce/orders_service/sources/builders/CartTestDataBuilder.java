package com.ecommerce.orders_service.sources.builders;

import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartItemDto;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.sources.scenarios.AddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.ControllerAddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.ControllerGetCartAuthScenario;
import com.ecommerce.orders_service.sources.scenarios.ControllerGetCartScenario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Data Builder fluido para construir instancias de {@link Cart} y sus DTOs asociados
 * sin constructores manuales ni valores quemados en los métodos de prueba.
 */
public class CartTestDataBuilder {

    private Long id;
    private Long userId;
    private boolean nullItems = false;
    private LocalDateTime createdDate = LocalDateTime.now();
    private LocalDateTime updatedDate = LocalDateTime.now();
    private final List<CartItem> items = new ArrayList<>();

    private CartTestDataBuilder() {}

    public static CartTestDataBuilder aCart() {
        return new CartTestDataBuilder();
    }

    public CartTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CartTestDataBuilder withUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public CartTestDataBuilder withNullItems(boolean nullItems) {
        this.nullItems = nullItems;
        return this;
    }

    public CartTestDataBuilder withItem(Long productId, Integer quantity, String price) {
        if (price != null && !price.isBlank()) {
            CartItem item = new CartItem();
            item.setId((long) (this.items.size() + 1));
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setPrice(new BigDecimal(price));
            this.items.add(item);
        }
        return this;
    }

    public Cart build() {
        Cart cart = new Cart();
        cart.setId(this.id);
        cart.setUserId(this.userId);
        cart.setCreatedDate(this.createdDate);
        cart.setUpdatedDate(this.updatedDate);
        if (this.nullItems) {
            cart.setItems(null);
        } else {
            for (CartItem item : this.items) {
                item.setCart(cart);
            }
            cart.setItems(new ArrayList<>(this.items));
        }
        return cart;
    }

    public static AddToCartRequest buildAddToCartRequest(ControllerAddToCartScenario scenario) {
        AddToCartRequest request = new AddToCartRequest();
        request.setUserId(scenario.userId());
        request.setProductId(scenario.productId());
        request.setQuantity(scenario.quantity());
        return request;
    }

    public static AddToCartRequest buildAddToCartRequest(AddToCartScenario scenario) {
        AddToCartRequest request = new AddToCartRequest();
        request.setUserId(scenario.userId());
        request.setProductId(scenario.requestProductId());
        request.setQuantity(scenario.requestQuantity());
        return request;
    }

    public static ProductResponse buildProductResponse(AddToCartScenario scenario) {
        ProductResponse response = new ProductResponse();
        response.setPrice(new BigDecimal(scenario.catalogPrice()));
        return response;
    }

    public static CartResponse buildCartResponse(ControllerAddToCartScenario scenario) {
        CartItemDto itemDto = new CartItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.price())
        );
        return new CartResponse(
                scenario.cartId(),
                scenario.userId(),
                List.of(itemDto),
                new BigDecimal(scenario.totalAmount())
        );
    }

    public static CartResponse buildCartResponse(ControllerGetCartScenario scenario) {
        CartItemDto itemDto = new CartItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.price())
        );
        return new CartResponse(
                scenario.cartId(),
                scenario.userId(),
                List.of(itemDto),
                new BigDecimal(scenario.totalAmount())
        );
    }

    public static CartResponse buildCartResponse(ControllerGetCartAuthScenario scenario) {
        CartItemDto itemDto = new CartItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.price())
        );
        return new CartResponse(
                scenario.cartId(),
                scenario.headerUserId(),
                List.of(itemDto),
                new BigDecimal(scenario.totalAmount())
        );
    }
}
