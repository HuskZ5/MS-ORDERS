package com.ecommerce.orders_service.repository;

import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.specification.OrderSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("h2")
class OrderHistoryRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldConnectToH2Database() {
        long totalOrders = orderRepository.count();

        assertThat(totalOrders).isZero();
    }

    @Test
    void shouldReturnOnlyOrdersFromRequestedUser() {

        Order user1Order = new Order(
                1L,
                new BigDecimal("150.00"),
                OrderStatus.CREATED
        );

        Order user2Order = new Order(
                2L,
                new BigDecimal("300.00"),
                OrderStatus.CREATED
        );

        orderRepository.save(user1Order);
        orderRepository.save(user2Order);
        orderRepository.flush();

        Page<Order> result = orderRepository.findAll(
                OrderSpecifications.hasUserId(1L),
                PageRequest.of(0, 5)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .allMatch(order -> order.getUserId().equals(1L));
    }

    @Test
    void shouldFilterOrderHistoryByStatus() {

        orderRepository.save(new Order(
                1L,
                new BigDecimal("150.00"),
                OrderStatus.CREATED
        ));

        orderRepository.save(new Order(
                1L,
                new BigDecimal("300.00"),
                OrderStatus.CANCELLED
        ));

        orderRepository.flush();

        Page<Order> result = orderRepository.findAll(
                OrderSpecifications.hasUserId(1L)
                        .and(OrderSpecifications.hasStatus(OrderStatus.CREATED)),
                PageRequest.of(0, 5)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);

        assertThat(result.getContent())
                .allMatch(order -> order.getStatus() == OrderStatus.CREATED);
    }

    @Test
    void shouldFilterOrderHistoryByDateRange() {

        Order order1 = orderRepository.save(new Order(
                1L,
                new BigDecimal("150.00"),
                OrderStatus.CREATED
        ));

        Order order2 = orderRepository.save(new Order(
                1L,
                new BigDecimal("200.00"),
                OrderStatus.CREATED
        ));

        Order order3 = orderRepository.save(new Order(
                1L,
                new BigDecimal("300.00"),
                OrderStatus.CREATED
        ));

        orderRepository.flush();

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                LocalDateTime.of(2026, 10, 1, 10, 0),
                order1.getId()
        );

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                LocalDateTime.of(2026, 10, 10, 15, 0),
                order2.getId()
        );

        jdbcTemplate.update(
                "UPDATE orders SET created_at = ? WHERE id = ?",
                LocalDateTime.of(2026, 10, 20, 12, 0),
                order3.getId()
        );

        LocalDate startDate = LocalDate.of(2026, 10, 10);
        LocalDate endDate = LocalDate.of(2026, 10, 15);

        Page<Order> result = orderRepository.findAll(
                OrderSpecifications.hasUserId(1L)
                        .and(OrderSpecifications.createdAfter(startDate.atStartOfDay()))
                        .and(OrderSpecifications.createdBefore(endDate.plusDays(1).atStartOfDay())),
                PageRequest.of(0, 5)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId())
                .isEqualTo(order2.getId());
    }

    @Test
    void shouldPaginateOrdersFromNewestToOldest() {

        for (int i = 0; i < 7; i++) {
            orderRepository.save(new Order(
                    1L,
                    new BigDecimal("100.00"),
                    OrderStatus.CREATED
            ));
        }

        orderRepository.flush();

        Page<Order> firstPage = orderRepository.findAll(
                OrderSpecifications.hasUserId(1L),
                PageRequest.of(
                        0,
                        5,
                        Sort.by(
                                Sort.Order.desc("createdAt"),
                                Sort.Order.desc("id")
                        )
                )
        );

        Page<Order> secondPage = orderRepository.findAll(
                OrderSpecifications.hasUserId(1L),
                PageRequest.of(
                        1,
                        5,
                        Sort.by(
                                Sort.Order.desc("createdAt"),
                                Sort.Order.desc("id")
                        )
                )
        );

        assertThat(firstPage.getContent()).hasSize(5);
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(7);

        assertThat(firstPage.getContent().get(0).getId())
                .isGreaterThan(firstPage.getContent().get(1).getId());

        assertThat(firstPage.getContent().get(4).getId())
                .isGreaterThan(secondPage.getContent().get(0).getId());
    }
}
