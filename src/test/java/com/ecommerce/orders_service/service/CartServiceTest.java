package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.client.ProductClient;
import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.sources.aggregators.AddToCartScenarioAggregator;
import com.ecommerce.orders_service.sources.aggregators.GetCartByUserIdScenarioAggregator;
import com.ecommerce.orders_service.sources.aggregators.GetCartScenarioAggregator;
import com.ecommerce.orders_service.sources.builders.CartTestDataBuilder;
import com.ecommerce.orders_service.sources.scenarios.AddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.GetCartByUserIdScenario;
import com.ecommerce.orders_service.sources.scenarios.GetCartScenario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas unitarias puras para {@link CartService}.
 * <p>
 * Sigue la regla de arquitectura <b>Zero Hardcoded Data</b>:
 * <ul>
 *   <li>Estructura jerárquica con {@link Nested} y {@link DisplayName}.</li>
 *   <li>Aserciones fluidas exclusivamente con AssertJ ({@code assertThat}).</li>
 *   <li>Pruebas parametrizadas alimentadas 100% desde {@link CsvFileSource} e inyectadas
 *       mediante {@link AggregateWith} hacia Records de escenario.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartService - Suite de Pruebas Unitarias (Zero Hardcoded Data)")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    @Nested
    @DisplayName("Consulta de Carrito por ID de Usuario (getCartByUserId)")
    class GetCartByUserIdTests {

        @ParameterizedTest(name = "{index} - {0}")
        @CsvFileSource(resources = "/csv/service_get_cart_by_user_id_scenarios.csv", numLinesToSkip = 1)
        @DisplayName("Escenarios de consulta o creación por defecto de carrito por userId desde CSV")
        void shouldHandleGetCartByUserIdScenarios(
                @AggregateWith(GetCartByUserIdScenarioAggregator.class) GetCartByUserIdScenario scenario) {

            if (scenario.cartExists()) {
                Cart existingCart = CartTestDataBuilder.aCart()
                        .withId(scenario.cartId())
                        .withUserId(scenario.userId())
                        .withNullItems(scenario.itemsNull())
                        .withItem(scenario.productId(), scenario.quantity(), scenario.price())
                        .build();
                when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.of(existingCart));
            } else {
                when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.empty());
            }

            CartResponse response = cartService.getCartByUserId(scenario.userId());

            assertThat(response).isNotNull();
            assertThat(response.getCartId()).isEqualTo(scenario.cartId());
            assertThat(response.getUserId()).isEqualTo(scenario.userId());
            assertThat(response.getItems()).hasSize(scenario.expectedItemsCount());
            assertThat(response.getTotalAmount()).isEqualByComparingTo(scenario.expectedTotalAmount());

            verify(cartRepository).findByUserId(scenario.userId());
        }
    }

    @Nested
    @DisplayName("Agregar Productos al Carrito (addItemToCart)")
    class AddItemToCartTests {

        @ParameterizedTest(name = "{index} - {0}")
        @CsvFileSource(resources = "/csv/service_add_to_cart_scenarios.csv", numLinesToSkip = 1)
        @DisplayName("Escenarios de inserción, acumulación y validación en carrito desde CSV")
        void shouldHandleAddToCartScenarios(
                @AggregateWith(AddToCartScenarioAggregator.class) AddToCartScenario scenario) throws Exception {

            AddToCartRequest request = CartTestDataBuilder.buildAddToCartRequest(scenario);

            if (scenario.expectedException() != null) {
                Throwable ex = scenario.expectedException()
                        .getDeclaredConstructor(String.class)
                        .newInstance(scenario.expectedErrorMessage());
                when(productClient.getProductById(scenario.requestProductId())).thenThrow(ex);

                assertThatThrownBy(() -> cartService.addItemToCart(request))
                        .isInstanceOf(scenario.expectedException())
                        .hasMessage(scenario.expectedErrorMessage());

                verify(productClient).getProductById(scenario.requestProductId());
                verify(cartRepository, never()).save(any(Cart.class));
            } else {
                if (scenario.cartExists()) {
                    Cart existingCart = CartTestDataBuilder.aCart()
                            .withId(scenario.cartId())
                            .withUserId(scenario.userId())
                            .withNullItems(scenario.itemsNull())
                            .withItem(scenario.existingProductId(), scenario.existingQuantity(), scenario.existingPrice())
                            .build();
                    when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.of(existingCart));
                } else {
                    when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.empty());
                }

                ProductResponse productResponse = CartTestDataBuilder.buildProductResponse(scenario);
                when(productClient.getProductById(scenario.requestProductId())).thenReturn(productResponse);
                when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

                CartResponse response = cartService.addItemToCart(request);

                assertThat(response).isNotNull();
                assertThat(response.getUserId()).isEqualTo(scenario.userId());
                assertThat(response.getItems()).hasSize(scenario.expectedItemsCount());
                assertThat(response.getTotalAmount()).isEqualByComparingTo(scenario.expectedTotalAmount());

                verify(productClient).getProductById(scenario.requestProductId());
                verify(cartRepository).findByUserId(scenario.userId());
                verify(cartRepository).save(any(Cart.class));
            }
        }
    }

    @Nested
    @DisplayName("Consulta de Carrito por Usuario Autenticado (getCarByAuthenticatedUser)")
    class GetCartTests {

        @ParameterizedTest(name = "{index} - {0}")
        @CsvFileSource(resources = "/csv/service_get_cart_scenarios.csv", numLinesToSkip = 1)
        @DisplayName("Escenarios de consulta de carrito por usuario autenticado desde CSV")
        void shouldHandleGetCartScenarios(
                @AggregateWith(GetCartScenarioAggregator.class) GetCartScenario scenario) {

            if (scenario.cartExists()) {
                Cart existingCart = CartTestDataBuilder.aCart()
                        .withId(scenario.cartId())
                        .withUserId(scenario.userId())
                        .withItem(scenario.productId(), scenario.quantity(), scenario.price())
                        .build();
                when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.of(existingCart));

                CartResponse response = cartService.getCarByAuthenticatedUser(scenario.userId());

                assertThat(response).isNotNull();
                assertThat(response.getCartId()).isEqualTo(scenario.cartId());
                assertThat(response.getUserId()).isEqualTo(scenario.userId());
                assertThat(response.getItems()).hasSize(scenario.expectedItemsCount());
                assertThat(response.getTotalAmount()).isEqualByComparingTo(scenario.expectedTotalAmount());
            } else {
                when(cartRepository.findByUserId(scenario.userId())).thenReturn(Optional.empty());

                assertThatThrownBy(() -> cartService.getCarByAuthenticatedUser(scenario.userId()))
                        .isInstanceOf(scenario.expectedException())
                        .hasMessage(scenario.expectedErrorMessage());
            }

            verify(cartRepository).findByUserId(scenario.userId());
        }
    }
}
