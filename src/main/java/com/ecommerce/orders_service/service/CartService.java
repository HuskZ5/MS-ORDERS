package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartItemDto;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.exceptions.CartNotFoundException;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.repository.CartRepository;
import com.ecommerce.orders_service.repository.OrderRepository;
import com.ecommerce.orders_service.client.ProductClient;
import com.ecommerce.orders_service.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    public CartService(CartRepository cartRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    /**
     * Obtiene el carrito activo asociado al identificador de usuario proporcionado.
     * Si no se encuentra un carrito persistido para dicho usuario, retorna una representación vacía.
     *
     * @param userId Identificador único del usuario.
     * @return {@link CartResponse} con el estado actual o vacío del carrito.
     */
    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setItems(new ArrayList<>());
                    return newCart;
                });

        return mapToCartResponse(cart);
    }

    /**
     * Agrega un ítem al carrito de compras de un usuario según los datos especificados en la solicitud.
     * <p>
     * <b>Regla de negocio implementada:</b>
     * <ul>
     *   <li>Verifica la existencia del carrito activo del usuario; en caso de no existir, se instancia uno nuevo.</li>
     *   <li>Inspecciona iterativamente los elementos actuales del carrito ({@code cart.getItems()}) utilizando
     *       la Stream API para validar si el {@code productId} solicitado ya se encuentra registrado.</li>
     *   <li><b>Si el producto ya existe:</b> No se crea un nuevo registro. En su lugar, se actualiza la entidad
     *       existente sumando la nueva cantidad a la cantidad acumulada previa (evitando inserciones duplicadas
     *       y conflictos de restricciones únicas en base de datos) y se actualiza el precio unitario vigente.</li>
     *   <li><b>Si el producto no existe:</b> Se instancia un nuevo {@link CartItem}, asociándole el carrito,
     *       el identificador del producto, la cantidad y el precio, y se añade a la colección de ítems.</li>
     * </ul>
     * Finalmente, actualiza la fecha de modificación del carrito, persiste los cambios y retorna la representación
     * mapeada en {@link CartResponse}.
     *
     * @param request DTO {@link AddToCartRequest} con los datos del ítem a agregar, incluyendo el ID del usuario,
     *                ID del producto, cantidad solicitada y precio unitario.
     * @return {@link CartResponse} que contiene la información actualizada del carrito, lista de ítems y monto total calculado.
     * @throws IllegalArgumentException si los datos requeridos en la solicitud son inválidos o nulos.
     */
    @Transactional
    public CartResponse addItemToCart(AddToCartRequest request) {
        ProductResponse product =
                productClient.getProductById(request.getProductId());
        BigDecimal productPrice = product.getPrice();

        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(request.getUserId());
                    newCart.setItems(new ArrayList<>());
                    newCart.setCreatedDate(LocalDateTime.now());
                    return newCart;
                });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // Buscar si el producto ya existe en el carrito
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId() != null && item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int currentQty = existingItem.getQuantity() != null ? existingItem.getQuantity() : 0;
            int addedQty = request.getQuantity() != null ? request.getQuantity() : 0;
            existingItem.setQuantity(currentQty + addedQty);
            existingItem.setPrice(productPrice);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(productPrice);
            cart.getItems().add(newItem);
        }

        cart.setUpdatedDate(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }


    /**
     * Devuelve el carrito de compras correspondiente a un usuario autenticado.
     *
     * @param userId Identificador único del usuario autenticado.
     * @return {@link CartResponse} con el detalle de ítems y total acumulado.
     * @throws CartNotFoundException si no existe ningún carrito asociado al identificador proporcionado.
     */
    @Transactional(readOnly = true)
    public CartResponse getCarByAuthenticatedUser(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("No se encontró carrito para el usuario " + userId));

        return mapToCartResponse(cart);
    }

    /**
     * Convierte la entidad {@link Cart} a su correspondiente DTO de transferencia {@link CartResponse}.
     * <p>
     * Transforma la lista de entidades {@link CartItem} a {@link CartItemDto} calculando de manera acumulativa
     * el monto total del carrito a partir del subtotal de cada ítem (cantidad &times; precio unitario).
     *
     * @param cart Entidad de dominio {@link Cart} a transformar.
     * @return Instancia de {@link CartResponse} con los datos estructurados para la respuesta de la API.
     */
    public CartResponse mapToCartResponse(Cart cart) {
        AtomicLong counter = new AtomicLong(1);
        List<CartItemDto> itemDtos = cart.getItems() != null ?
                cart.getItems().stream()
                        .map(item -> new CartItemDto(
                                counter.getAndIncrement(),
                                /*item.getId(),*/
                                item.getProductId(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .collect(Collectors.toList()) : new ArrayList<>();

        BigDecimal total = itemDtos.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getUserId(), itemDtos, total);
    }
}