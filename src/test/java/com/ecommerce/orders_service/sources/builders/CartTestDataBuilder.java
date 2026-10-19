package com.ecommerce.orders_service.sources.builders;

import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.ApplyCouponRequest;
import com.ecommerce.orders_service.dto.CartItemDto;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.sources.scenarios.cart.AddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerAddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerApplyCouponScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerGetCartAuthScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerGetCartScenario;

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

    public CartTestDataBuilder withMultipleItems(Integer linesCount, Long baseProductId, Integer quantity, String price) {
        int count = linesCount != null ? linesCount : 0;
        if (count == 1) {
            return withItem(baseProductId, quantity, price);
        }
        if (count > 1 && price != null && !price.isBlank()) {
            long startId = baseProductId != null ? baseProductId : 1L;
            for (int i = 0; i < count; i++) {
                withItem(startId + i, quantity, price);
            }
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

    public static ApplyCouponRequest buildApplyCouponRequest(ControllerApplyCouponScenario scenario) {
        return new ApplyCouponRequest(scenario.couponCode());
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
        BigDecimal total = new BigDecimal(scenario.totalAmount());
        return new CartResponse(
                scenario.cartId(),
                scenario.userId(),
                List.of(itemDto),
                total,
                BigDecimal.ZERO,
                total
        );
    }

    public static CartResponse buildCartResponse(ControllerGetCartScenario scenario) {
        CartItemDto itemDto = new CartItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.price())
        );
        BigDecimal total = new BigDecimal(scenario.totalAmount());
        return new CartResponse(
                scenario.cartId(),
                scenario.userId(),
                List.of(itemDto),
                total,
                BigDecimal.ZERO,
                total
        );
    }

    public static CartResponse buildCartResponse(ControllerGetCartAuthScenario scenario) {
        CartItemDto itemDto = new CartItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.price())
        );
        BigDecimal total = new BigDecimal(scenario.totalAmount());
        return new CartResponse(
                scenario.cartId(),
                scenario.headerUserId(),
                List.of(itemDto),
                total,
                BigDecimal.ZERO,
                total
        );
    }

    public static CartResponse buildCartResponse(ControllerApplyCouponScenario scenario) {
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
                new BigDecimal(scenario.subtotal()),
                new BigDecimal(scenario.discountAmount()),
                new BigDecimal(scenario.totalAmount())
        );
    }
}
