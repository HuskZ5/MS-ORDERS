package com.ecommerce.orders_service.controllers;

import com.ecommerce.orders_service.config.WebControllerTest;
import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.ApplyCouponRequest;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.service.CartService;
import com.ecommerce.orders_service.service.CouponService;
import com.ecommerce.orders_service.sources.builders.CartTestDataBuilder;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerAddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerApplyCouponScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerGetCartAuthScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerGetCartScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
 *   <li>Casos de prueba parametrizados mediante {@link MethodSource} apuntando a {@code CartScenarioProvider}.</li>
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

    @MockitoBean
    private CouponService couponService;

    @Nested
    @DisplayName("POST /api/carts/items (HU-11)")
    class AddProductToCartTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.CartScenarioProvider#provideControllerAddToCartScenarios")
        @DisplayName("Escenarios web de agregar producto al carrito")
        void shouldHandleAddProductToCartScenarios(ControllerAddToCartScenario scenario) throws Exception {

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
        @MethodSource("com.ecommerce.orders_service.sources.providers.CartScenarioProvider#provideControllerGetCartScenarios")
        @DisplayName("Escenarios web de consulta de carrito por userId")
        void shouldHandleGetCartScenarios(ControllerGetCartScenario scenario) throws Exception {

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
        @MethodSource("com.ecommerce.orders_service.sources.providers.CartScenarioProvider#provideControllerGetCartAuthScenarios")
        @DisplayName("Escenarios web de consulta de carrito autenticado")
        void shouldHandleGetCartAuthScenarios(ControllerGetCartAuthScenario scenario) throws Exception {

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

    @Nested
    @DisplayName("POST /api/carts/{userId}/coupons (Aplicación de cupón al carrito)")
    class ApplyCouponTests {

        @ParameterizedTest(name = "{index} - {0}")
        @MethodSource("com.ecommerce.orders_service.sources.providers.CartScenarioProvider#provideControllerApplyCouponScenarios")
        @DisplayName("Escenarios web de aplicación de cupón por userId")
        void shouldHandleApplyCouponScenarios(ControllerApplyCouponScenario scenario) throws Exception {

            ApplyCouponRequest request = CartTestDataBuilder.buildApplyCouponRequest(scenario);
            String jsonPayload = objectMapper.writeValueAsString(request);

            if (scenario.exceptionClass() != null) {
                Throwable ex = scenario.exceptionClass()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                doThrow(ex).when(couponService).applyCoupon(scenario.userId(), scenario.couponCode());

                mockMvc.perform(post("/api/carts/{userId}/coupons", scenario.userId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.error").value(scenario.expectedErrorMessage()));
            } else {
                CartResponse response = CartTestDataBuilder.buildCartResponse(scenario);
                doNothing().when(couponService).applyCoupon(scenario.userId(), scenario.couponCode());
                when(cartService.getCarByAuthenticatedUser(scenario.userId())).thenReturn(response);

                mockMvc.perform(post("/api/carts/{userId}/coupons", scenario.userId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonPayload))
                        .andExpect(status().is(scenario.expectedHttpStatus()))
                        .andExpect(jsonPath("$.cartId").value(scenario.cartId()))
                        .andExpect(jsonPath("$.userId").value(scenario.userId()))
                        .andExpect(jsonPath("$.subtotal").value(Double.parseDouble(scenario.subtotal())))
                        .andExpect(jsonPath("$.discountAmount").value(Double.parseDouble(scenario.discountAmount())))
                        .andExpect(jsonPath("$.totalAmount").value(Double.parseDouble(scenario.totalAmount())))
                        .andExpect(jsonPath("$.items[0].itemId").value(scenario.itemId()))
                        .andExpect(jsonPath("$.items[0].productId").value(scenario.productId()))
                        .andExpect(jsonPath("$.items[0].quantity").value(scenario.quantity()))
                        .andExpect(jsonPath("$.items[0].price").value(Double.parseDouble(scenario.price())));

                verify(couponService).applyCoupon(scenario.userId(), scenario.couponCode());
                verify(cartService).getCarByAuthenticatedUser(scenario.userId());
            }
        }
    }
}
