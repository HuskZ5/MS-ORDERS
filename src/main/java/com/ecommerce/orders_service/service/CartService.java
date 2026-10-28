package com.ecommerce.orders_service.service;

import com.ecommerce.orders_service.client.ProductClient;
import com.ecommerce.orders_service.dto.AddToCartRequest;
import com.ecommerce.orders_service.dto.CartItemDto;
import com.ecommerce.orders_service.dto.CartResponse;
import com.ecommerce.orders_service.dto.ProductResponse;
import com.ecommerce.orders_service.dto.UpdateCartItemQuantityRequest;
import com.ecommerce.orders_service.exceptions.BusinessOperationException;
import com.ecommerce.orders_service.exceptions.CartNotFoundException;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import com.ecommerce.orders_service.model.Cart;
import com.ecommerce.orders_service.model.CartItem;
import com.ecommerce.orders_service.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final CouponService couponService;

    public CartService(CartRepository cartRepository, ProductClient productClient, CouponService couponService) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.couponService = couponService;
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
     *
     * @param request DTO {@link AddToCartRequest} con los datos del ítem a agregar.
     * @return {@link CartResponse} que contiene la información actualizada del carrito.
     */
    @Transactional
    public CartResponse addItemToCart(AddToCartRequest request) {
        ProductResponse product =
                productClient.getProductById(request.productId());
        BigDecimal productPrice = product.getPrice();

        Cart cart = cartRepository.findByUserId(request.userId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(request.userId());
                    newCart.setItems(new ArrayList<>());
                    newCart.setCreatedDate(LocalDateTime.now());
                    return newCart;
                });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // Buscar si el producto ya existe en el carrito
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId() != null && item.getProductId().equals(request.productId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int currentQty = existingItem.getQuantity() != null ? existingItem.getQuantity() : 0;
            int addedQty = request.quantity() != null ? request.quantity() : 0;
            int newQty = currentQty + addedQty;
            if (newQty > 5) throw new BusinessOperationException("Máximo 5 unidades por producto");
            existingItem.setQuantity(currentQty + addedQty);
            existingItem.setPrice(productPrice);
        } else {
            if (cart.getItems().size() >= 10) {
                throw new BusinessOperationException("Máximo 10 líneas de productos diferentes");
            }
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(request.productId());
            if (request.quantity() > 5) {
                throw new BusinessOperationException("Máximo 5 unidades por producto");
            }
            newItem.setQuantity(request.quantity());
            newItem.setPrice(productPrice);
            cart.getItems().add(newItem);
        }
        // Calcular el nuevo total
        BigDecimal newTotal = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validar límite de $500
        if (newTotal.compareTo(new BigDecimal("5000")) > 0) {
            throw new BusinessOperationException("El total del carrito no puede superar $500");
        }

        cart.setUpdatedDate(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    

    /**
     * Actualiza la cantidad de un ítem existente en el carrito (PATCH /api/v1/cart/items/{itemId}).
     *
     * @param itemId  Identificador único del ítem en la tabla cart_items.
     * @param request Record {@link UpdateCartItemQuantityRequest} con la nueva cantidad solicitada.
     * @return {@link CartResponse} con el carrito recalculado en el backend.
     */
    @Transactional
    public CartResponse updateItemQuantity(Long itemId, UpdateCartItemQuantityRequest request) {
        Cart cart = cartRepository.findByItems_Id(itemId)
                .orElseThrow(() -> new CartNotFoundException("No se encontró el ítem en el carrito con id: " + itemId));

        CartItem targetItem = cart.getItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("No se encontró el ítem en el carrito con id: " + itemId));

        if (request.quantity() > 5) {
            throw new BusinessOperationException("Máximo 5 unidades por producto");
        }

        ProductResponse product = productClient.getProductById(targetItem.getProductId());
        if (product.getStock() != null && product.getStock() < request.quantity()) {
            throw new ProductOutOfStockException("Stock insuficiente para el producto " + targetItem.getProductId());
        }

        targetItem.setQuantity(request.quantity());
        targetItem.setPrice(product.getPrice());

        BigDecimal newTotal = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (newTotal.compareTo(new BigDecimal("5000")) > 0) {
            throw new BusinessOperationException("El total del carrito no puede superar $500");
        }

        cart.setUpdatedDate(LocalDateTime.now());
        Cart savedCart = cartRepository.save(cart);

        return mapToCartResponse(savedCart);
    }

    /**
     * Retira un producto del carrito eliminando su línea correspondiente (DELETE /api/v1/cart/items/{itemId}).
     *
     * @param itemId Identificador único del ítem en la tabla cart_items.
     */
    @Transactional
    public void removeItemFromCart(Long itemId) {
        Cart cart = cartRepository.findByItems_Id(itemId)
                .orElseThrow(() -> new CartNotFoundException("No se encontró el ítem en el carrito con id: " + itemId));

        boolean removed = cart.getItems().removeIf(item -> itemId.equals(item.getId()));
        if (!removed) {
            throw new CartNotFoundException("No se encontró el ítem en el carrito con id: " + itemId);
        }

        cart.setUpdatedDate(LocalDateTime.now());
        cartRepository.save(cart);
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
     * Mapea el {@code item.getId()} real persistido en base de datos y calcula subtotales y totales con {@link BigDecimal}.
     *
     * @param cart Entidad de dominio {@link Cart} a transformar.
     * @return Instancia de {@link CartResponse} con los datos estructurados para la respuesta de la API.
     */
    public CartResponse mapToCartResponse(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems() != null ?
                cart.getItems().stream()
                        .map(item -> new CartItemDto(
                                item.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .collect(Collectors.toList()) : new ArrayList<>();

        BigDecimal subtotal = itemDtos.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = cart.getId() != null
                ? couponService.calculateDiscount(cart.getId(), subtotal)
                : BigDecimal.ZERO;

        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                itemDtos,
                subtotal,
                discountAmount,
                totalAmount
        );
    }
}
