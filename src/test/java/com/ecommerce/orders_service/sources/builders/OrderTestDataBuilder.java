package com.ecommerce.orders_service.sources.builders;

import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.ItemReturned;
import com.ecommerce.orders_service.dto.OrderItemDto;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderItem;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.sources.scenarios.order.CancelOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerCancelOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerCreateOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetAllOrdersScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetTodayOrdersScenario;
import com.ecommerce.orders_service.sources.scenarios.order.CreateOrderScenario;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test Data Builder fluido para construir instancias de {@link Order} y sus DTOs asociados
 * desacoplando las pruebas de constructores crudos o setters individuales.
 */
public class OrderTestDataBuilder {

    private Long id = 1L;
    private Long userId = 10L;
    private BigDecimal total = new BigDecimal("250.00");
    private OrderStatus status = OrderStatus.CREATED;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime cancelledAt = null;
    private List<OrderItem> items = new ArrayList<>();

    private OrderTestDataBuilder() {}

    public static OrderTestDataBuilder anOrder() {
        return new OrderTestDataBuilder();
    }

    public OrderTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public OrderTestDataBuilder withUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public OrderTestDataBuilder withTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public OrderTestDataBuilder withTotal(String total) {
        this.total = new BigDecimal(total);
        return this;
    }

    public OrderTestDataBuilder withStatus(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderTestDataBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public OrderTestDataBuilder withCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
        return this;
    }

    public OrderTestDataBuilder withItem(Long productId, Integer quantity, String unitPrice) {
        OrderItem item = new OrderItem();
        item.setId((long) (this.items.size() + 1));
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        this.items.add(item);
        return this;
    }

    public Order build() {
        Order order = new Order(this.userId, this.total, this.status);
        order.setId(this.id);
        order.setCreatedAt(this.createdAt);
        order.setCancelledAt(this.cancelledAt);
        for (OrderItem item : this.items) {
            item.setOrder(order);
        }
        order.setItems(new ArrayList<>(this.items));
        return order;
    }

    public static ProductResponse buildProductResponse(CreateOrderScenario scenario) {
        ProductResponse response = new ProductResponse();
        response.setPrice(new BigDecimal(scenario.catalogPrice()));
        response.setStock(scenario.catalogStock());
        return response;
    }

    public static List<OrderResponse> buildOrderResponseList(ControllerGetAllOrdersScenario scenario) {
        OrderItemDto item = new OrderItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.unitPrice())
        );
        OrderResponse response = new OrderResponse(
                scenario.orderId(),
                scenario.userId(),
                new BigDecimal(scenario.total()),
                scenario.status(),
                LocalDateTime.now(),
                List.of(item)
        );
        return List.of(response);
    }

    public static List<OrderResponse> buildOrderResponseList(ControllerGetTodayOrdersScenario scenario) {
        OrderItemDto item = new OrderItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.unitPrice())
        );
        OrderResponse response = new OrderResponse(
                scenario.orderId(),
                scenario.userId(),
                new BigDecimal(scenario.total()),
                scenario.status(),
                LocalDateTime.now(),
                List.of(item)
        );
        return List.of(response);
    }

    public static OrderResponse buildOrderResponse(ControllerGetOrderScenario scenario) {
        OrderItemDto item = new OrderItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.unitPrice())
        );
        return new OrderResponse(
                scenario.orderId(),
                scenario.userId(),
                new BigDecimal(scenario.total()),
                scenario.status(),
                LocalDateTime.now(),
                List.of(item)
        );
    }

    public static OrderResponse buildOrderResponse(ControllerCreateOrderScenario scenario) {
        OrderItemDto item = new OrderItemDto(
                scenario.itemId(),
                scenario.productId(),
                scenario.quantity(),
                new BigDecimal(scenario.unitPrice())
        );
        return new OrderResponse(
                scenario.orderId(),
                scenario.userId(),
                new BigDecimal(scenario.total()),
                scenario.status(),
                LocalDateTime.now(),
                List.of(item)
        );
    }

    public static CancelOrderRequest buildCancelOrderRequest(ControllerCancelOrderScenario scenario) {
        return CancelOrderRequest.builder()
                .orderId(scenario.orderId())
                .userId(scenario.userId())
                .build();
    }

    public static CancelOrderRequest buildCancelOrderRequest(CancelOrderScenario scenario) {
        return CancelOrderRequest.builder()
                .orderId(scenario.orderId())
                .userId(scenario.requesterUserId())
                .build();
    }

    public static CancelOrderResponse buildCancelOrderResponse(ControllerCancelOrderScenario scenario) {
        Long effectiveOrderId = scenario.orderId() != null ? scenario.orderId() : scenario.pathId();
        ItemReturned itemReturned = ItemReturned.builder()
                .productId(scenario.returnedProductId())
                .quantity(scenario.returnedQuantity())
                .build();

        return CancelOrderResponse.builder()
                .orderId(effectiveOrderId)
                .userId(scenario.userId())
                .estatus(scenario.expectedStatus())
                .refundAmount(new BigDecimal(scenario.refundAmount()))
                .itemsReturned(List.of(itemReturned))
                .cancelledAt(LocalDateTime.now())
                .build();
    }
}
