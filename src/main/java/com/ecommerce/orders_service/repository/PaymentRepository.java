
package com.ecommerce.orders_service.repository;

import com.ecommerce.orders_service.model.Payment;
import com.ecommerce.orders_service.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOperationId(String operationId);

    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
