
package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.PaymentRequest;
import com.ecommerce.orders_service.dto.PaymentResponse;
import com.ecommerce.orders_service.exceptions.InvalidOrderStateException;
import com.ecommerce.orders_service.exceptions.InvalidPaymentException;
import com.ecommerce.orders_service.exceptions.UnauthorizedOrderAccessException;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.model.Payment;
import com.ecommerce.orders_service.model.PaymentStatus;
import com.ecommerce.orders_service.repository.OrderRepository;
import com.ecommerce.orders_service.repository.PaymentRepository;
import com.ecommerce.orders_service.sources.builders.OrderTestDataBuilder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService - Pruebas unitarias HU-08")
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Nested
    @DisplayName("Procesamiento de pagos")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Debe procesar el pago de una orden válida")
        void shouldProcessPaymentSuccessfully() {

            // Arrange: preparar los datos de prueba
            Order order = OrderTestDataBuilder.anOrder().build();

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-test-success");

            when(orderRepository.findByIdForUpdate(order.getId()))
                    .thenReturn(Optional.of(order));

            when(paymentRepository.findByOperationId(
                    request.getOperationId()))
                    .thenReturn(Optional.empty());

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> {
                        Payment payment = invocation.getArgument(0);
                        payment.setId(1L);
                        return payment;
                    });

            // Act: ejecutar el pago
            PaymentResponse response = paymentService.processPayment(
                    order.getId(),
                    order.getUserId(),
                    request
            );

            // Assert: comprobar los resultados
            assertThat(response.getPaymentStatus())
                    .isEqualTo(PaymentStatus.SUCCESS);

            assertThat(response.getOrderStatus())
                    .isEqualTo(OrderStatus.PAID);

            assertThat(response.getAmount())
                    .isEqualByComparingTo(order.getTotal());

            assertThat(response.getOperationId())
                    .isEqualTo(request.getOperationId());

            assertThat(order.getStatus())
                    .isEqualTo(OrderStatus.PAID);

            verify(paymentRepository).save(any(Payment.class));
            verify(orderRepository).save(order);
        }


        @Test
        @DisplayName("No debe permitir pagar una orden cancelada")
        void shouldRejectPaymentForCancelledOrder() {

            // Arrange: preparar una orden cancelada
            Order order = OrderTestDataBuilder.anOrder()
                    .withStatus(OrderStatus.CANCELLED)
                    .build();

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-cancelled-order");

            when(orderRepository.findByIdForUpdate(order.getId()))
                    .thenReturn(Optional.of(order));

            // Act y Assert: verificar que el pago sea rechazado
            assertThatThrownBy(() -> paymentService.processPayment(
                    order.getId(),
                    order.getUserId(),
                    request
            ))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessage("No se puede pagar una orden cancelada");

            // Verificar que no se registre ningún pago
            verify(paymentRepository, never()).save(any(Payment.class));
            verify(orderRepository, never()).save(any(Order.class));
        }


        @Test
        @DisplayName("No debe registrar dos veces la misma operación")
        void shouldNotDuplicatePaymentOnRetry() {

            // Arrange: preparar una orden ya pagada
            Order order = OrderTestDataBuilder.anOrder()
                    .withStatus(OrderStatus.PAID)
                    .build();

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-retry-001");

            Payment existingPayment = new Payment();
            existingPayment.setId(1L);
            existingPayment.setOrder(order);
            existingPayment.setOperationId(request.getOperationId());
            existingPayment.setAmount(order.getTotal());
            existingPayment.setStatus(PaymentStatus.SUCCESS);

            when(orderRepository.findByIdForUpdate(order.getId()))
                    .thenReturn(Optional.of(order));

            when(paymentRepository.findByOperationId(
                    request.getOperationId()))
                    .thenReturn(Optional.of(existingPayment));

            // Act: repetir la operación
            PaymentResponse response = paymentService.processPayment(
                    order.getId(),
                    order.getUserId(),
                    request
            );

            // Assert: devolver el pago existente
            assertThat(response.getPaymentId())
                    .isEqualTo(existingPayment.getId());

            assertThat(response.getPaymentStatus())
                    .isEqualTo(PaymentStatus.SUCCESS);

            assertThat(response.getOrderStatus())
                    .isEqualTo(OrderStatus.PAID);

            assertThat(response.getMessage())
                    .isEqualTo("Esta operación ya fue registrada");

            // No debe guardar otro pago ni actualizar nuevamente la orden
            verify(paymentRepository, never()).save(any(Payment.class));
            verify(orderRepository, never()).save(any(Order.class));
        }


        @Test
        @DisplayName("No debe permitir pagar nuevamente una orden pagada")
        void shouldRejectSecondPaymentWithDifferentOperationId() {

            // Arrange: orden que ya fue pagada
            Order order = OrderTestDataBuilder.anOrder()
                    .withStatus(OrderStatus.PAID)
                    .build();

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-new-attempt");

            when(orderRepository.findByIdForUpdate(order.getId()))
                    .thenReturn(Optional.of(order));

            when(paymentRepository.findByOperationId(
                    request.getOperationId()))
                    .thenReturn(Optional.empty());

            // Act y Assert: rechazar el nuevo intento
            assertThatThrownBy(() -> paymentService.processPayment(
                    order.getId(),
                    order.getUserId(),
                    request
            ))
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessage("La orden ya fue pagada o procesada");

            // No debe registrar otro pago
            verify(paymentRepository, never()).save(any(Payment.class));
            verify(orderRepository, never()).save(any(Order.class));
        }


        @Test
        @DisplayName("No debe permitir pagar una orden de otro usuario")
        void shouldRejectPaymentFromAnotherUser() {

            // Arrange: preparar una orden válida
            Order order = OrderTestDataBuilder.anOrder()
                    .withStatus(OrderStatus.CREATED)
                    .build();

            Long anotherUserId = order.getUserId() + 1L;

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-unauthorized-user");

            when(orderRepository.findByIdForUpdate(order.getId()))
                    .thenReturn(Optional.of(order));

            // Act y Assert: rechazar al usuario no autorizado
            assertThatThrownBy(() -> paymentService.processPayment(
                    order.getId(),
                    anotherUserId,
                    request
            ))
                    .isInstanceOf(UnauthorizedOrderAccessException.class)
                    .hasMessage(
                            "No tienes autorización para pagar esta orden"
                    );

            // Verificar que no se registre ningún pago
            verify(paymentRepository, never()).save(any(Payment.class));
            verify(orderRepository, never()).save(any(Order.class));
        }


        @Test
        @DisplayName("Debe rechazar el pago cuando el usuario no está autenticado")
        void shouldRejectPaymentWhenUserIsNotAuthenticated() {

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-no-auth-001");

            assertThatThrownBy(() ->
                    paymentService.processPayment(1L, null, request)
            )
                    .isInstanceOf(UnauthorizedUserException.class)
                    .hasMessage("Usuario no autenticado");

            verifyNoInteractions(orderRepository, paymentRepository);
        }


        @Test
        @DisplayName("Debe rechazar un identificador de operación vacío")
        void shouldRejectPaymentWithBlankOperationId() {

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("");

            assertThatThrownBy(() ->
                    paymentService.processPayment(1L, 10L, request)
            )
                    .isInstanceOf(InvalidPaymentException.class)
                    .hasMessage("Los datos de la operación son obligatorios");

            verifyNoInteractions(orderRepository, paymentRepository);
        }


        @Test
        @DisplayName("Debe rechazar un operationId utilizado en otra orden")
        void shouldRejectOperationIdUsedByAnotherOrder() {

            Long orderId = 2L;
            Long userId = 10L;

            Order order = OrderTestDataBuilder.anOrder()
                    .withId(orderId)
                    .withUserId(userId)
                    .withStatus(OrderStatus.CREATED)
                    .build();

            Order previousOrder = OrderTestDataBuilder.anOrder()
                    .withId(1L)
                    .build();

            Payment previousPayment = new Payment();
            previousPayment.setOrder(previousOrder);
            previousPayment.setOperationId("payment-duplicate-001");
            previousPayment.setStatus(PaymentStatus.SUCCESS);

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-duplicate-001");

            when(orderRepository.findByIdForUpdate(orderId))
                    .thenReturn(Optional.of(order));

            when(paymentRepository.findByOperationId(request.getOperationId()))
                    .thenReturn(Optional.of(previousPayment));

            assertThatThrownBy(() ->
                    paymentService.processPayment(orderId, userId, request)
            )
                    .isInstanceOf(InvalidPaymentException.class)
                    .hasMessage("El identificador de operación pertenece a otra orden");

            verify(paymentRepository, never()).save(any(Payment.class));
            verify(orderRepository, never()).save(any(Order.class));
        }


        @Test
        @DisplayName("Debe rechazar el pago de una orden entregada")
        void shouldRejectPaymentForDeliveredOrder() {

            Long orderId = 2L;
            Long userId = 10L;

            Order order = OrderTestDataBuilder.anOrder()
                    .withId(orderId)
                    .withUserId(userId)
                    .withStatus(OrderStatus.DELIVERED)
                    .build();

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-delivered-001");

            when(orderRepository.findByIdForUpdate(orderId))
                    .thenReturn(Optional.of(order));

            assertThatThrownBy(() ->
                    paymentService.processPayment(orderId, userId, request)
            )
                    .isInstanceOf(InvalidOrderStateException.class)
                    .hasMessage("La orden ya fue pagada o procesada");

            verify(paymentRepository, never()).save(any(Payment.class));
            verify(orderRepository, never()).save(any(Order.class));
        }


        @ParameterizedTest
        @EnumSource(
                value = OrderStatus.class,
                names = {"PENDING", "CONFIRMED"}
        )
        @DisplayName("Debe permitir pagar órdenes en estados válidos")
        void shouldAllowPaymentForValidOrderStates(OrderStatus status) {

            Long orderId = 1L;
            Long userId = 10L;

            Order order = OrderTestDataBuilder.anOrder()
                    .withId(orderId)
                    .withUserId(userId)
                    .withStatus(status)
                    .build();

            PaymentRequest request = new PaymentRequest();
            request.setOperationId("payment-valid-state-001");

            when(orderRepository.findByIdForUpdate(orderId))
                    .thenReturn(Optional.of(order));

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            PaymentResponse response =
                    paymentService.processPayment(orderId, userId, request);

            assertThat(response.getPaymentStatus())
                    .isEqualTo(PaymentStatus.SUCCESS);

            assertThat(order.getStatus())
                    .isEqualTo(OrderStatus.PAID);

            verify(paymentRepository).save(any(Payment.class));
            verify(orderRepository).save(order);
        }






    }
}
