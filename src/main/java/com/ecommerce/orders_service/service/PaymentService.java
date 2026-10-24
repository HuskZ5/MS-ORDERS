
package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.PaymentRequest;
import com.ecommerce.orders_service.dto.PaymentResponse;
import com.ecommerce.orders_service.exceptions.*;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.model.Payment;
import com.ecommerce.orders_service.model.PaymentStatus;
import com.ecommerce.orders_service.repository.OrderRepository;
import com.ecommerce.orders_service.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public PaymentResponse processPayment(
            Long orderId,
            Long authenticatedUserId,
            PaymentRequest request) {

        // 1. Validar autenticación
        if (authenticatedUserId == null) {
            throw new UnauthorizedUserException(
                    "Usuario no autenticado");
        }

        if (orderId == null || request == null
                || request.getOperationId() == null
                || request.getOperationId().isBlank()) {
            throw new InvalidPaymentException(
                    "Los datos de la operación son obligatorios");
        }

        String operationId = request.getOperationId();

        // 2. Bloquear la orden durante la transacción
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Orden no encontrada con id: " + orderId));

        // 3. Verificar que pertenezca al usuario
        if (!order.getUserId().equals(authenticatedUserId)) {
            throw new UnauthorizedOrderAccessException(
                    "No tienes autorización para pagar esta orden");
        }

        // 4. Detectar reintentos de la misma operación
        Payment previousPayment = paymentRepository
                .findByOperationId(operationId)
                .orElse(null);

        if (previousPayment != null) {
            if (!previousPayment.getOrder().getId().equals(orderId)) {
                throw new InvalidPaymentException(
                        "El identificador de operación pertenece a otra orden");
            }

            return buildResponse(
                    previousPayment,
                    order,
                    "Esta operación ya fue registrada");
        }

        // 5. Validar estados permitidos
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "No se puede pagar una orden cancelada");
        }

        if (order.getStatus() == OrderStatus.PAID
                || order.getStatus() == OrderStatus.SHIPPED
                || order.getStatus() == OrderStatus.DELIVERED
                || paymentRepository.existsByOrderIdAndStatus(
                        orderId, PaymentStatus.SUCCESS)) {
            throw new InvalidOrderStateException(
                    "La orden ya fue pagada o procesada");
        }

        if (order.getStatus() != OrderStatus.CREATED
                && order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStateException(
                    "El estado de la orden no permite realizar pagos");
        }

        // 6. Registrar el pago simulado
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setOperationId(operationId);
        payment.setAmount(order.getTotal());
        payment.setStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        // 7. Actualizar el estado de la orden
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return buildResponse(
                savedPayment,
                order,
                "Pago simulado procesado correctamente");
    }

    private PaymentResponse buildResponse(
            Payment payment,
            Order order,
            String message) {

        return new PaymentResponse(
                payment.getId(),
                order.getId(),
                payment.getOperationId(),
                payment.getAmount(),
                payment.getStatus(),
                order.getStatus(),
                payment.getCreatedAt(),
                message
        );
    }
}
