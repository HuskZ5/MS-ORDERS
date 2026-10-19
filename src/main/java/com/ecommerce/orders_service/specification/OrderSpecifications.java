package com.ecommerce.orders_service.specification;

import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class OrderSpecifications {

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) ->
                status == null
                        ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> createdAfter(LocalDateTime startDate) {
        return (root, query, cb) ->
                startDate == null
                        ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(
                        root.get("createdAt"), startDate);
    }

    public static Specification<Order> createdBefore(LocalDateTime endDate) {
        return (root, query, cb) ->
                endDate == null
                        ? cb.conjunction()
                        : cb.lessThan(root.get("createdAt"), endDate);
    }
}
