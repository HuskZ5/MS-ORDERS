package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.config.WebControllerTest;
import com.ecommerce.orders_service.dto.CancelOrderRequest;
import com.ecommerce.orders_service.dto.CancelOrderResponse;
import com.ecommerce.orders_service.dto.OrderResponse;
import com.ecommerce.orders_service.exceptions.UnauthorizedOrderAccessException;
import com.ecommerce.orders_service.service.OrderService;
import com.ecommerce.orders_service.sources.builders.OrderTestDataBuilder;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerCancelOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerCreateOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetAllOrdersScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetOrderScenario;
import com.ecommerce.orders_service.sources.scenarios.order.ControllerGetTodayOrdersScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de capa web (WebMvc slice) para {@link OrderController}.
 * <p>
 * Implementa la regla estricta <b>Zero Hardcoded Data</b>:
 * <ul>
 *   <li>Casos de prueba parametrizados mediante {@link MethodSource} e inyectados con Records desde {@code OrderScenarioProvider}.</li>
 *   <li>Serialización JSON delegada a {@link ObjectMapper}.</li>
 *   <li>Construcción de DTOs centralizada en {@link OrderTestDataBuilder} sin operadores {@code new} en el test.</li>
 * </ul>
 */
@WebControllerTest(controllers = OrderController.class)
@DisplayName("OrderController - Pruebas de Capa Web MVC (Zero Hardcoded Data)")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Nested
    @DisplayName("GET /api/orders (Listado general de pedidos)")
    class GetAllOrdersTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideControllerGetAllOrdersScenarios")
        @DisplayName("Escenarios web de listado de todas las órdenes")
        void shouldHandleGetAllOrdersScenarios(ControllerGetAllOrdersScenario scenario) throws Exception {

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(orderService.getAllOrders()).thenThrow(ex);

                mockMvc.perform(get("/api/orders"))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.message").value(scenario.expectedErrorMessage()));
            } else {
                List<OrderResponse> responses = OrderTestDataBuilder.buildOrderResponseList(scenario);
                when(orderService.getAllOrders()).thenReturn(responses);

                mockMvc.perform(get("/api/orders"))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$[0].id").value(scenario.orderId()))
                        .andExpect(jsonPath("$[0].userId").value(scenario.userId()))
                        .andExpect(jsonPath("$[0].total").value(Double.parseDouble(scenario.total())))
                        .andExpect(jsonPath("$[0].status").value(scenario.status()))
                        .andExpect(jsonPath("$[0].items[0].productId").value(scenario.productId()))
                        .andExpect(jsonPath("$[0].items[0].quantity").value(scenario.quantity()))
                        .andExpect(jsonPath("$[0].items[0].unitPrice").value(Double.parseDouble(scenario.unitPrice())));
            }
        }
    }

    @Nested
    @DisplayName("GET /api/orders/today (Listado de pedidos del día)")
    class GetTodayOrdersTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideControllerGetTodayOrdersScenarios")
        @DisplayName("Escenarios web de listado de órdenes del día")
        void shouldHandleGetTodayOrdersScenarios(ControllerGetTodayOrdersScenario scenario) throws Exception {

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(orderService.getTodayOrders()).thenThrow(ex);

                mockMvc.perform(get("/api/orders/today"))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.message").value(scenario.expectedErrorMessage()));
            } else {
                List<OrderResponse> responses = OrderTestDataBuilder.buildOrderResponseList(scenario);
                when(orderService.getTodayOrders()).thenReturn(responses);

                mockMvc.perform(get("/api/orders/today"))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$[0].id").value(scenario.orderId()))
                        .andExpect(jsonPath("$[0].userId").value(scenario.userId()))
                        .andExpect(jsonPath("$[0].total").value(Double.parseDouble(scenario.total())))
                        .andExpect(jsonPath("$[0].status").value(scenario.status()))
                        .andExpect(jsonPath("$[0].items[0].productId").value(scenario.productId()))
                        .andExpect(jsonPath("$[0].items[0].quantity").value(scenario.quantity()))
                        .andExpect(jsonPath("$[0].items[0].unitPrice").value(Double.parseDouble(scenario.unitPrice())));
            }
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id} (HU-14)")
    class GetOrderTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideControllerGetOrderScenarios")
        @DisplayName("Escenarios web de consulta de pedido por ID")
        void shouldHandleGetOrderScenarios(ControllerGetOrderScenario scenario) throws Exception {

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            String.valueOf(scenario.userId()),
                            null,
                            java.util.Collections.emptyList()
                    );

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(orderService.getOrderById(scenario.orderId(), scenario.userId())).thenThrow(ex);

                mockMvc.perform(get("/api/orders/{id}", scenario.orderId()).principal(authentication))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.message").value(scenario.expectedErrorMessage()));
            } else {
                OrderResponse response = OrderTestDataBuilder.buildOrderResponse(scenario);
                when(orderService.getOrderById(scenario.orderId(), scenario.userId())).thenReturn(response);

                mockMvc.perform(get("/api/orders/{id}", scenario.orderId()).principal(authentication))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.id").value(scenario.orderId()))
                        .andExpect(jsonPath("$.userId").value(scenario.userId()))
                        .andExpect(jsonPath("$.total").value(Double.parseDouble(scenario.total())))
                        .andExpect(jsonPath("$.status").value(scenario.status()))
                        .andExpect(jsonPath("$.items[0].productId").value(scenario.productId()))
                        .andExpect(jsonPath("$.items[0].quantity").value(scenario.quantity()))
                        .andExpect(jsonPath("$.items[0].unitPrice").value(Double.parseDouble(scenario.unitPrice())));
            }
        }

        @Test
        @DisplayName("Retorna 401 cuando el usuario no está autenticado")
        void shouldReturn401WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(get("/api/orders/{id}", 15L))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value("Usuario no autenticado"));

            verifyNoInteractions(orderService);
        }

        @Test
        @DisplayName("Retorna 403 cuando la orden pertenece a otro usuario")
        void shouldReturn403WhenUserDoesNotOwnOrder() throws Exception {

            Long orderId = 15L;
            Long authenticatedUserId = 2L;

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            String.valueOf(authenticatedUserId),
                            null,
                            java.util.Collections.emptyList()
                    );

            when(orderService.getOrderById(orderId, authenticatedUserId))
                    .thenThrow(new UnauthorizedOrderAccessException(
                            "No tienes autorización para consultar esta orden"
                    ));

            mockMvc.perform(
                            get("/api/orders/{id}", orderId)
                                    .principal(authentication)
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message")
                            .value("No tienes autorización para consultar esta orden"));
        }
    }

    @Nested
    @DisplayName("POST /api/orders (HU-03)")
    class CreateOrderTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideControllerCreateOrderScenarios")
        @DisplayName("Escenarios web de creación de orden desde carrito")
        void shouldHandleCreateOrderScenarios(ControllerCreateOrderScenario scenario) throws Exception {

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(orderService.createOrderFromCart(scenario.userId())).thenThrow(ex);

                mockMvc.perform(post("/api/orders")
                                .param("userId", String.valueOf(scenario.userId())))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.message").value(scenario.expectedErrorMessage()));
            } else {
                OrderResponse response = OrderTestDataBuilder.buildOrderResponse(scenario);
                when(orderService.createOrderFromCart(scenario.userId())).thenReturn(response);

                mockMvc.perform(post("/api/orders")
                                .param("userId", String.valueOf(scenario.userId())))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.id").value(scenario.orderId()))
                        .andExpect(jsonPath("$.userId").value(scenario.userId()))
                        .andExpect(jsonPath("$.total").value(Double.parseDouble(scenario.total())))
                        .andExpect(jsonPath("$.status").value(scenario.status()))
                        .andExpect(jsonPath("$.items[0].productId").value(scenario.productId()))
                        .andExpect(jsonPath("$.items[0].quantity").value(scenario.quantity()))
                        .andExpect(jsonPath("$.items[0].unitPrice").value(Double.parseDouble(scenario.unitPrice())));
            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/orders/cancel y /api/orders/{id}/cancel (HU-07)")
    class CancelOrderTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.OrderScenarioProvider#provideControllerCancelOrderScenarios")
        @DisplayName("Escenarios web de cancelación de pedido")
        void shouldHandleCancelOrderScenarios(ControllerCancelOrderScenario scenario) throws Exception {

            CancelOrderRequest request = OrderTestDataBuilder.buildCancelOrderRequest(scenario);
            String jsonPayload = objectMapper.writeValueAsString(request);

            UsernamePasswordAuthenticationToken authentication = null;

            if (scenario.userId() != null) {
                authentication = new UsernamePasswordAuthenticationToken(
                        String.valueOf(scenario.userId()),
                        null,
                        java.util.Collections.emptyList()
                );
            }

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(orderService.cancelOrder(any(CancelOrderRequest.class))).thenThrow(ex);
            } else if (scenario.expectedErrorMessage() == null || scenario.expectedErrorMessage().isBlank()) {
                CancelOrderResponse response = OrderTestDataBuilder.buildCancelOrderResponse(scenario);
                when(orderService.cancelOrder(any(CancelOrderRequest.class))).thenReturn(response);
            }

            MockHttpServletRequestBuilder requestBuilder = scenario.pathId() != null
                    ? patch("/api/orders/{id}/cancel", scenario.pathId())
                    : patch("/api/orders/cancel");

            requestBuilder.contentType(MediaType.APPLICATION_JSON).content(jsonPayload);

            if (authentication != null) {
                requestBuilder.principal(authentication);
            }

            if (scenario.expectedErrorMessage() != null && !scenario.expectedErrorMessage().isBlank()) {
                mockMvc.perform(requestBuilder)
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.message").value(scenario.expectedErrorMessage()));
            } else {
                Long expectedOrderId = scenario.orderId() != null ? scenario.orderId() : scenario.pathId();
                mockMvc.perform(requestBuilder)
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.orderId").value(expectedOrderId))
                        .andExpect(jsonPath("$.userId").value(scenario.userId()))
                        .andExpect(jsonPath("$.estatus").value(scenario.expectedStatus().name()))
                        .andExpect(jsonPath("$.refundAmount").value(Double.parseDouble(scenario.refundAmount())))
                        .andExpect(jsonPath("$.itemsReturned[0].productId").value(scenario.returnedProductId()))
                        .andExpect(jsonPath("$.itemsReturned[0].quantity").value(scenario.returnedQuantity()));
            }
        }
    }
}