package com.ecommerce.orders_service.repository;

import com.ecommerce.orders_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findByUserId(Long userId);

    List<Order> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}