package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.client.ProductClient;
import com.ecommerce.orders_service.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para la capa de servicio {@link CartService}.
 * <p>
 * Verifica la lógica de negocio del carrito de compras, asegurando la correcta manipulación
 * de cantidades acumuladas, inserción de nuevos productos y manejo del ciclo de vida del carrito
 * mediante simulaciones con JUnit 5 y Mockito.
 */
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    /**
     * Valida que al agregar un producto que ya existe previamente en el carrito del usuario,
     * se incremente su cantidad acumulada sin duplicar registros ni alterar la unicidad de las líneas.
     * <p>
     * <b>Estructura de la prueba (Triple-A Pattern):</b>
     * <ul>
     *   <li><b>Arrange (Configuración inicial):</b>
     *     <ul>
     *       <li>Se define un usuario (ID: 1L) y un producto preexistente (ID: 100L) con cantidad inicial de 2 unidades a $50.00.</li>
     *       <li>Se inicializa el {@link Cart} con un {@link CartItem} que representa dicho producto existente.</li>
     *       <li>Se crea un {@link AddToCartRequest} solicitando añadir 3 unidades adicionales del mismo producto (ID: 100L) al mismo precio.</li>
     *       <li>Se configuran los mocks de {@link CartRepository} para retornar el carrito existente y responder al guardado.</li>
     *     </ul>
     *   </li>
     *   <li><b>Act (Ejecución):</b>
     *     <ul>
     *       <li>Se invoca el método bajo prueba {@link CartService#addItemToCart(AddToCartRequest)}.</li>
     *     </ul>
     *   </li>
     *   <li><b>Assert (Verificaciones y aserciones):</b>
     *     <ul>
     *       <li>Se valida que el {@link CartResponse} no sea nulo.</li>
     *       <li>Se verifica que el tamaño de la lista de ítems siga siendo exactamente 1 (garantiza la ausencia de duplicados).</li>
     *       <li>Se comprueba que la cantidad resultante sea la suma exacta (2 + 3 = 5 unidades).</li>
     *       <li>Se comprueba que el precio unitario e ID del producto se mantengan consistentes.</li>
     *       <li>Se afirma que el importe total acumulado coincida con el cálculo matemático (5 &times; $50.00 = $250.00).</li>
     *       <li>Se verifica la interacción con {@link CartRepository} asegurando que se invoque {@code save} exactamente una vez.</li>
     *     </ul>
     *   </li>
     * </ul>
     */
    @Test
    void shouldIncreaseQuantityWithoutDuplicatingWhenProductAlreadyExistsInCart() {
        // Arrange
        Long userId = 1L;
        Long productId = 100L;

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());

        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setCart(cart);
        existingItem.setProductId(productId);
        existingItem.setQuantity(2);
        existingItem.setPrice(new BigDecimal("50.00"));
        cart.getItems().add(existingItem);

        AddToCartRequest request = new AddToCartRequest();
        request.setUserId(userId);
        request.setProductId(productId);
        request.setQuantity(3);
        ProductResponse productResponse = new ProductResponse();
        productResponse.setPrice(new BigDecimal("50.00"));

        when(productClient.getProductById(productId)).thenReturn(productResponse);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CartResponse response = cartService.addItemToCart(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getItems().size(), "No debe duplicar el ítem en el carrito");
        assertEquals(5, response.getItems().get(0).getQuantity(), "La cantidad debe ser la suma (2 + 3 = 5)");
        assertEquals(productId, response.getItems().get(0).getProductId());
        assertEquals(new BigDecimal("50.00"), response.getItems().get(0).getPrice());
        assertEquals(new BigDecimal("250.00"), response.getTotalAmount(), "Total calculado debe ser 5 * 50.00 = 250.00");

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(cart);
        verify(productClient, times(1)).getProductById(productId);
    }

    /**
     * Valida que al agregar un producto diferente a los existentes en el carrito,
     * se añada como un nuevo ítem a la colección.
     */
    @Test
    void shouldAddNewItemWhenProductDoesNotExistInCart() {
        // Arrange
        Long userId = 1L;
        Long existingProductId = 100L;
        Long newProductId = 200L;

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());

        CartItem existingItem = new CartItem();
        existingItem.setId(1L);
        existingItem.setCart(cart);
        existingItem.setProductId(existingProductId);
        existingItem.setQuantity(2);
        existingItem.setPrice(new BigDecimal("50.00"));
        cart.getItems().add(existingItem);

        AddToCartRequest request = new AddToCartRequest();
        request.setUserId(userId);
        request.setProductId(newProductId);
        request.setQuantity(1);
        ProductResponse productResponse = new ProductResponse();
        productResponse.setPrice(new BigDecimal("30.00"));

        when(productClient.getProductById(newProductId))
                .thenReturn(productResponse);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CartResponse response = cartService.addItemToCart(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getItems().size(), "Debe contener 2 productos distintos");
        assertEquals(new BigDecimal("130.00"), response.getTotalAmount(), "Total: (2 * 50.00) + (1 * 30.00) = 130.00");

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(cart);
    }

    /**
     * Valida que cuando un usuario no posee un carrito registrado, se cree automáticamente
     * un nuevo carrito y se asigne el primer producto.
     */
    @Test
    void shouldCreateNewCartAndAddItemWhenCartDoesNotExistForUser() {
        // Arrange
        Long userId = 2L;
        Long productId = 300L;

        AddToCartRequest request = new AddToCartRequest();
        request.setUserId(userId);
        request.setProductId(productId);
        request.setQuantity(4);
        ProductResponse productResponse = new ProductResponse();
        productResponse.setPrice(new BigDecimal("25.00"));

        when(productClient.getProductById(productId))
                .thenReturn(productResponse);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CartResponse response = cartService.addItemToCart(request);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(4, response.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("100.00"), response.getTotalAmount(), "Total: 4 * 25.00 = 100.00");

        verify(cartRepository, times(1)).findByUserId(userId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }
}
