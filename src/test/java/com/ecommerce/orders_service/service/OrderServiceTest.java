package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.client.ProductClient;
import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.UnauthorizedOrderAccessException;
import com.ecommerce.orders_service.exceptions.UnauthorizedUserException;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.Order;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.repository.OrderRepository;
import com.ecommerce.orders_service.sources.builders.CartTestDataBuilder;
import com.ecommerce.orders_service.sources.builders.OrderTestDataBuilder;
import com.ecommerce.orders_service.sources.scenarios.order.CancelOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.CreateOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.GetAllOrdersScenario;
import com.ecommerce.orders_service.sources.scenarios.order.GetOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.GetTodayOrdersScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas unitarias puras para {@link OrderService}.
 * <p>
 * Implementa la arquitectura estricta <b>Zero Hardcoded Data</b>:
 * <ul>
 *   <li>Estructura jerárquica con {@link Nested} y {@link DisplayName}.</li>
 *   <li>Aserciones fluidas exclusivamente con AssertJ ({@code assertThat}).</li>
 *   <li>Uso de {@link OrderTestDataBuilder} y {@link CartTestDataBuilder}.</li>
 *   <li>Pruebas parametrizadas alimentadas desde {@code OrderScenarioProvider} mediante {@link MethodSource}.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - Suite de Pruebas Unitarias (Zero Hardcoded Data)")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CouponService couponService;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("Listado General de Pedidos (getAllOrders)")
    class GetAllOrdersTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideGetAllOrdersScenarios")
        @DisplayName("Escenarios de listado general de órdenes")
        void shouldHandleGetAllOrdersScenarios(GetAllOrdersScenario scenario) {

            if (scenario.hasOrders()) {
                Order order = OrderTestDataBuilder.anOrder()
                        .withId(scenario.orderId())
                        .withUserId(scenario.userId())
                        .withTotal(scenario.total())
                        .withStatus(scenario.status())
                        .withItem(scenario.productId(), scenario.quantity(), scenario.unitPrice())
                        .build();

                when(orderRepository.findAll()).thenReturn(List.of(order));

                List<OrderResponse> responses = orderService.getAllOrders();

                assertThat(responses)
                        .isNotNull()
                        .hasSize(scenario.expectedOrdersCount())
                        .first()
                        .satisfies(response -> {
                            assertThat(response.getId()).isEqualTo(scenario.orderId());
                            assertThat(response.getUserId()).isEqualTo(scenario.userId());
                            assertThat(response.getTotal()).isEqualByComparingTo(scenario.total());
                            assertThat(response.getStatus()).isEqualTo(scenario.status().name());
                        });
            } else {
                when(orderRepository.findAll()).thenReturn(Collections.emptyList());

                assertThatThrownBy(() -> orderService.getAllOrders())
                        .isInstanceOf(scenario.expectedException())
                        .hasMessage(scenario.expectedErrorMessage());
            }

            verify(orderRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Listado de Pedidos del Día (getTodayOrders)")
    class GetTodayOrdersTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideGetTodayOrdersScenarios")
        @DisplayName("Escenarios de listado de órdenes del día")
        void shouldHandleGetTodayOrdersScenarios(GetTodayOrdersScenario scenario) {

            if (scenario.hasOrders()) {
                Order order = OrderTestDataBuilder.anOrder()
                        .withId(scenario.orderId())
                        .withUserId(scenario.userId())
                        .withTotal(scenario.total())
                        .withStatus(scenario.status())
                        .withItem(scenario.productId(), scenario.quantity(), scenario.unitPrice())
                        .build();

                when(orderRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(order));

                List<OrderResponse> responses = orderService.getTodayOrders();

                assertThat(responses)
                        .isNotNull()
                        .hasSize(scenario.expectedOrdersCount())
                        .first()
                        .satisfies(response -> {
                            assertThat(response.getId()).isEqualTo(scenario.orderId());
                            assertThat(response.getUserId()).isEqualTo(scenario.userId());
                            assertThat(response.getTotal()).isEqualByComparingTo(scenario.total());
                            assertThat(response.getStatus()).isEqualTo(scenario.status().name());
                        });
            } else {
                when(orderRepository.findByCreatedAtBetween(any(), any())).thenReturn(Collections.emptyList());

                assertThatThrownBy(() -> orderService.getTodayOrders())
                        .isInstanceOf(scenario.expectedException())
                        .hasMessage(scenario.expectedErrorMessage());
            }

            verify(orderRepository).findByCreatedAtBetween(any(), any());
        }
    }

    @Nested
    @DisplayName("Consulta de Pedidos (HU-14 / getOrderById)")
    class GetOrderByIdTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideGetOrderScenarios")
        @DisplayName("Escenarios de consulta de pedido por ID")
        void shouldHandleGetOrderScenarios(GetOrderScenario scenario) {

            if (scenario.orderExists()) {
                Order order = OrderTestDataBuilder.anOrder()
                        .withId(scenario.orderId())
                        .withUserId(scenario.userId())
                        .withTotal(scenario.total())
                        .withStatus(scenario.status())
                        .withItem(scenario.productId(), scenario.quantity(), scenario.unitPrice())
                        .build();

                when(orderRepository.findById(scenario.orderId())).thenReturn(Optional.of(order));

                OrderResponse response = orderService.getOrderById(scenario.orderId(), scenario.userId());

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(scenario.orderId());
                assertThat(response.getUserId()).isEqualTo(scenario.userId());
                assertThat(response.getTotal()).isEqualByComparingTo(scenario.total());
                assertThat(response.getStatus()).isEqualTo(scenario.status().name());
                assertThat(response.getItems())
                        .hasSize(1)
                        .first()
                        .satisfies(item -> {
                            assertThat(item.getProductId()).isEqualTo(scenario.productId());
                            assertThat(item.getQuantity()).isEqualTo(scenario.quantity());
                            assertThat(item.getUnitPrice()).isEqualByComparingTo(scenario.unitPrice());
                        });
            } else {
                when(orderRepository.findById(scenario.orderId())).thenReturn(Optional.empty());

                assertThatThrownBy(() -> orderService.getOrderById(scenario.orderId(), scenario.userId()))
                        .isInstanceOf(scenario.expectedException())
                        .hasMessage(scenario.expectedErrorMessage());
            }

            verify(orderRepository).findById(scenario.orderId());
        }

        @Test
        @DisplayName("No permite consultar una orden de otro usuario")
        void shouldRejectOrderFromAnotherUser() {

            Long orderId = 15L;
            Long orderOwnerId = 10L;
            Long authenticatedUserId = 7L;

            Order order = OrderTestDataBuilder.anOrder()
                    .withId(orderId)
                    .withUserId(orderOwnerId)
                    .build();

            when(orderRepository.findById(orderId))
                    .thenReturn(Optional.of(order));

            assertThatThrownBy(() ->
                    orderService.getOrderById(orderId, authenticatedUserId)
            )
                    .isInstanceOf(UnauthorizedOrderAccessException.class)
                    .hasMessage("No tienes autorización para consultar esta orden");

            verify(orderRepository).findById(orderId);
        }

        @Test
        @DisplayName("No permite consultar una orden sin usuario autenticado")
        void shouldRejectUnauthenticatedUser() {

            Long orderId = 15L;

            assertThatThrownBy(() ->
                    orderService.getOrderById(orderId, null)
            )
                    .isInstanceOf(UnauthorizedUserException.class)
                    .hasMessage("Usuario no autenticado");

            verify(orderRepository, never()).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("Creación de Pedidos desde Carrito (HU-03 / createOrderFromCart)")
    class CreateOrderTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideCreateOrderScenarios")
        @DisplayName("Escenarios de creación de orden desde carrito")
        void shouldHandleCreateOrderScenarios(CreateOrderScenario scenario) throws Exception {

            if (!scenario.cartExists()) {
                when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.empty());
            } else {
                CartTestDataBuilder cartBuilder = CartTestDataBuilder.aCart()
                        .withUserId(scenario.userId());
                if (!scenario.cartEmpty()) {
                    cartBuilder.withItem(scenario.productId(), scenario.quantity(), scenario.unitPrice());
                }
                Cart cart = cartBuilder.build();
                when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.of(cart));

                if (scenario.stockExceptionClass() != null) {
                    Throwable stockEx = scenario.stockExceptionClass()
                            .getDeclaredConstructor(String.class)
                            .newInstance(scenario.expectedErrorMessage());
                    doThrow(stockEx).when(productClient).reduceStock(scenario.productId(), scenario.quantity());
                } else if (scenario.expectedExceptionClass() == null) {
                    when(couponService.calculateFinalTotal(any(), any())).thenAnswer(inv -> inv.getArgument(1));
                    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
                    when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
                }
            }

            if (scenario.expectedExceptionClass() != null) {
                assertThatThrownBy(() -> orderService.createOrderFromCart(scenario.userId()))
                        .isInstanceOf(scenario.expectedExceptionClass())
                        .hasMessage(scenario.expectedErrorMessage());

                verify(orderRepository, never()).save(any(Order.class));
                verify(cartRepository, never()).save(any(Cart.class));
            } else {
                OrderResponse response = orderService.createOrderFromCart(scenario.userId());

                assertThat(response).isNotNull();
                assertThat(response.getUserId()).isEqualTo(scenario.userId());
                assertThat(response.getTotal()).isEqualByComparingTo(scenario.expectedTotal());
                assertThat(response.getStatus()).isEqualTo(scenario.expectedStatus().name());
                verify(productClient).reduceStock(scenario.productId(), scenario.quantity());
                verify(orderRepository).save(any(Order.class));
                verify(cartRepository).save(any(Cart.class));
            }
        }
    }

    @Nested
    @DisplayName("Cancelación de Pedidos (HU-15 / cancelOrder)")
    class CancelOrderTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideCancelOrderScenarios")
        @DisplayName("Escenarios de la máquina de estados con Record CancelOrderScenario")
        void shouldHandleCancelOrderStateMachineScenarios(CancelOrderScenario scenario) {

            if (!scenario.orderExists()) {
                when(orderRepository.findById(scenario.orderId())).thenReturn(Optional.empty());
            } else {
                Order order = OrderTestDataBuilder.anOrder()
                        .withId(scenario.orderId())
                        .withUserId(scenario.orderUserId())
                        .withStatus(scenario.initialStatus())
                        .withTotal(scenario.total())
                        .withItem(scenario.productId(), scenario.quantity(), scenario.unitPrice())
                        .build();

                when(orderRepository.findById(scenario.orderId())).thenReturn(Optional.of(order));
                if (scenario.shouldSucceed()) {
                    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
                }
            }

            CancelOrderRequest request = OrderTestDataBuilder.buildCancelOrderRequest(scenario);

            if (scenario.shouldSucceed()) {
                CancelOrderResponse response = orderService.cancelOrder(request);

                assertThat(response)
                        .isNotNull()
                        .extracting(
                                CancelOrderResponse::getOrderId,
                                CancelOrderResponse::getUserId,
                                CancelOrderResponse::getEstatus
                        )
                        .containsExactly(scenario.orderId(), scenario.orderUserId(), scenario.expectedStatus());

                assertThat(response.getRefundAmount()).isEqualByComparingTo(scenario.total());
                assertThat(response.getCancelledAt()).isNotNull();
                assertThat(response.getItemsReturned())
                        .hasSize(1)
                        .first()
                        .satisfies(item -> {
                            assertThat(item.getProductId()).isEqualTo(scenario.productId());
                            assertThat(item.getQuantity()).isEqualTo(scenario.quantity());
                        });

                verify(productClient).restoreStock(scenario.productId(), scenario.quantity());
                verify(orderRepository).save(any(Order.class));
            } else {
                assertThatThrownBy(() -> orderService.cancelOrder(request))
                        .isInstanceOf(scenario.expectedException())
                        .hasMessage(scenario.expectedErrorMessage());

                verify(productClient, never()).restoreStock(anyLong(), anyInt());
                verify(orderRepository, never()).save(any(Order.class));
            }
        }
    }
}