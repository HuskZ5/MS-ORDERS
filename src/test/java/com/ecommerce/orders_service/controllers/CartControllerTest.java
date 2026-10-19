package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.config.WebControllerTest;
import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.service.CartService;
import com.ecommerce.orders_service.sources.aggregators.ControllerAddToCartScenarioAggregator;
import com.ecommerce.orders_service.sources.aggregators.ControllerGetCartAuthScenarioAggregator;
import com.ecommerce.orders_service.sources.aggregators.ControllerGetCartScenarioAggregator;
import com.ecommerce.orders_service.sources.builders.CartTestDataBuilder;
import com.ecommerce.orders_service.sources.scenarios.ControllerAddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.ControllerGetCartAuthScenario;
import com.ecommerce.orders_service.sources.scenarios.ControllerGetCartScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de capa web (WebMvc slice) para {@link CartController} (HU-11).
 * <p>
 * Implementa la regla estricta <b>Zero Hardcoded Data</b>:
 * <ul>
 *   <li>Casos de prueba parametrizados mediante {@link CsvFileSource} e inyectados con {@link AggregateWith}.</li>
 *   <li>Serialización JSON delegada a {@link ObjectMapper}.</li>
 *   <li>Construcción de DTOs centralizada en {@link CartTestDataBuilder} sin operadores {@code new} en el test.</li>
 * </ul>
 */
@WebControllerTest(controllers = CartController.class)
@DisplayName("CartController - Pruebas de Capa Web MVC (Zero Hardcoded Data)")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    @Nested
    @DisplayName("POST /api/carts/items (HU-11)")
    class AddProductToCartTests {

        @ParameterizedTest(name = "{index} - {0}")
        @CsvFileSource(resources = "/csv/controller_add_to_cart_scenarios.csv", numLinesToSkip = 1)
        @DisplayName("Escenarios web de agregar producto al carrito desde CSV")
        void shouldHandleAddProductToCartScenarios(
                @AggregateWith(ControllerAddToCartScenarioAggregator.class) ControllerAddToCartScenario scenario) throws Exception {

            AddToCartRequest request = CartTestDataBuilder.buildAddToCartRequest(scenario);
            String jsonPayload = objectMapper.writeValueAsString(request);

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(cartService.addItemToCart(any(AddToCartRequest.class))).thenThrow(ex);

                mockMvc.perform(post("/api/carts/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.error").value(scenario.expectedErrorMessage()));
            } else {
                CartResponse response = CartTestDataBuilder.buildCartResponse(scenario);
                when(cartService.addItemToCart(any(AddToCartRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/carts/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.cartId").value(scenario.cartId()))
                        .andExpect(jsonPath("$.userId").value(scenario.userId()))
                        .andExpect(jsonPath("$.totalAmount").value(Double.parseDouble(scenario.totalAmount())))
                        .andExpect(jsonPath("$.items[0].itemId").value(scenario.itemId()))
                        .andExpect(jsonPath("$.items[0].productId").value(scenario.productId()))
                        .andExpect(jsonPath("$.items[0].quantity").value(scenario.quantity()))
                        .andExpect(jsonPath("$.items[0].price").value(Double.parseDouble(scenario.price())));
            }
        }
    }

    @Nested
    @DisplayName("GET /api/carts/{userId} (Consulta de carrito por usuario)")
    class GetCartTests {

        @ParameterizedTest(name = "{index} - {0}")
        @CsvFileSource(resources = "/csv/controller_get_cart_scenarios.csv", numLinesToSkip = 1)
        @DisplayName("Escenarios web de consulta de carrito por userId desde CSV")
        void shouldHandleGetCartScenarios(
                @AggregateWith(ControllerGetCartScenarioAggregator.class) ControllerGetCartScenario scenario) throws Exception {

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(cartService.getCarByAuthenticatedUser(scenario.userId())).thenThrow(ex);

                mockMvc.perform(get("/api/carts/{userId}", scenario.userId()))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.error").value(scenario.expectedErrorMessage()));
            } else {
                CartResponse response = CartTestDataBuilder.buildCartResponse(scenario);
                when(cartService.getCarByAuthenticatedUser(scenario.userId())).thenReturn(response);

                mockMvc.perform(get("/api/carts/{userId}", scenario.userId()))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.cartId").value(scenario.cartId()))
                        .andExpect(jsonPath("$.userId").value(scenario.userId()))
                        .andExpect(jsonPath("$.totalAmount").value(Double.parseDouble(scenario.totalAmount())))
                        .andExpect(jsonPath("$.items[0].itemId").value(scenario.itemId()))
                        .andExpect(jsonPath("$.items[0].productId").value(scenario.productId()))
                        .andExpect(jsonPath("$.items[0].quantity").value(scenario.quantity()))
                        .andExpect(jsonPath("$.items[0].price").value(Double.parseDouble(scenario.price())));
            }
        }
    }

    @Nested
    @DisplayName("GET /api/carts/me (Consulta de carrito por usuario autenticado con X-USER-ID)")
    class GetCartAuthTests {

        @ParameterizedTest(name = "{index} - {0}")
        @CsvFileSource(resources = "/csv/controller_get_cart_auth_scenarios.csv", numLinesToSkip = 1)
        @DisplayName("Escenarios web de consulta de carrito autenticado desde CSV")
        void shouldHandleGetCartAuthScenarios(
                @AggregateWith(ControllerGetCartAuthScenarioAggregator.class) ControllerGetCartAuthScenario scenario) throws Exception {

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(cartService.getCarByAuthenticatedUser(scenario.headerUserId())).thenThrow(ex);
            } else if (scenario.expectedErrorMessage() == null || scenario.expectedErrorMessage().isBlank()) {
                CartResponse response = CartTestDataBuilder.buildCartResponse(scenario);
                when(cartService.getCarByAuthenticatedUser(scenario.headerUserId())).thenReturn(response);
            }

            MockHttpServletRequestBuilder requestBuilder = get("/api/carts/me");
            if (scenario.headerUserId() != null) {
                requestBuilder.header("X-USER-ID", scenario.headerUserId());
            }

            if (scenario.expectedErrorMessage() != null && !scenario.expectedErrorMessage().isBlank()) {
                mockMvc.perform(requestBuilder)
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.error").value(scenario.expectedErrorMessage()));
            } else {
                mockMvc.perform(requestBuilder)
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.cartId").value(scenario.cartId()))
                        .andExpect(jsonPath("$.userId").value(scenario.headerUserId()))
                        .andExpect(jsonPath("$.totalAmount").value(Double.parseDouble(scenario.totalAmount())))
                        .andExpect(jsonPath("$.items[0].itemId").value(scenario.itemId()))
                        .andExpect(jsonPath("$.items[0].productId").value(scenario.productId()))
                        .andExpect(jsonPath("$.items[0].quantity").value(scenario.quantity()))
                        .andExpect(jsonPath("$.items[0].price").value(Double.parseDouble(scenario.price())));
            }
        }
    }
}
