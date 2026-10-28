package com.ecommerce.orders_service.sources.providers;

import com.ecommerce.orders_service.exceptions.BusinessOperationException;
import com.ecommerce.orders_service.exceptions.CartEmptyException;
import com.ecommerce.orders_service.exceptions.CartNotFoundException;
import com.ecommerce.orders_service.exceptions.CouponNotFoundException;
import com.ecommerce.orders_service.exceptions.InvalidCartOperationException;
import com.ecommerce.orders_service.exceptions.InvalidCouponException;
import com.ecommerce.orders_service.exceptions.ProductNotFoundException;
import com.ecommerce.orders_service.exceptions.ProductOutOfStockException;
import com.ecommerce.orders_service.sources.scenarios.cart.AddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerAddToCartScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerApplyCouponScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerGetCartAuthScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerGetCartScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerRemoveCartItemScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.ControllerUpdateCartItemQuantityScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.GetCartByUserIdScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.GetCartScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.RemoveCartItemScenario;
import com.ecommerce.orders_service.sources.scenarios.cart.UpdateCartItemQuantityScenario;

import java.util.stream.Stream;

/**
 * Proveedor centralizado de escenarios de prueba para el dominio de Carrito ({@code CartController} y {@code CartService}).
 * <p>
 * Mantiene la regla <b>Zero Hardcoded Data</b> fuera de las clases de prueba y garantiza seguridad de tipos en tiempo de compilación.
 */
public final class CartScenarioProvider {

    private CartScenarioProvider() {}

    // =========================================================================
    // CartController Scenarios
    // =========================================================================

    public static Stream<ControllerAddToCartScenario> provideControllerAddToCartScenarios() {
        return Stream.of(
                new ControllerAddToCartScenario(
                        "Agrega producto exitosamente al carrito y retorna 200 OK",
                        10L, 1L, 1L, 101L, 2, "50.00", "100.00", 200, null, null
                ),
                new ControllerAddToCartScenario(
                        "Falla al agregar producto inexistente y retorna 404 Not Found",
                        null, 1L, null, 999L, 1, null, null, 404,
                        ProductNotFoundException.class, "Producto no encontrado con id: 999"
                ),
                new ControllerAddToCartScenario(
                        "Falla al agregar producto sin stock disponible y retorna 409 Conflict",
                        null, 1L, null, 101L, 5, null, null, 409,
                        ProductOutOfStockException.class, "Stock insuficiente para el producto 101"
                ),
                new ControllerAddToCartScenario(
                        "Falla al agregar cantidad invalida al carrito y retorna 400 Bad Request",
                        null, 1L, null, 101L, 0, null, null, 400,
                        InvalidCartOperationException.class, "La cantidad solicitada debe ser mayor a cero"
                )
        );
    }

    public static Stream<ControllerUpdateCartItemQuantityScenario> provideControllerUpdateCartItemQuantityScenarios() {
        return Stream.of(
                new ControllerUpdateCartItemQuantityScenario(
                        "Actualiza exitosamente la cantidad del item en el carrito y retorna 200 OK",
                        10L, 3, 1L, 5L, 101L, "50.00", "150.00", 200, null, null, null
                ),
                new ControllerUpdateCartItemQuantityScenario(
                        "Falla validacion Bean Validation cuando la cantidad es menor a 1 y retorna 400 Bad Request",
                        10L, 0, null, null, null, null, null, 400,
                        null, "VALIDATION_ERROR", null
                ),
                new ControllerUpdateCartItemQuantityScenario(
                        "Falla al actualizar cantidad cuando el item no existe y retorna 404 Not Found",
                        999L, 2, null, null, null, null, null, 404,
                        CartNotFoundException.class, "CART_NOT_FOUND",
                        "No se encontró el ítem en el carrito con id: 999"
                ),
                new ControllerUpdateCartItemQuantityScenario(
                        "Falla al actualizar cantidad cuando el stock es insuficiente y retorna 409 Conflict",
                        10L, 4, null, null, null, null, null, 409,
                        ProductOutOfStockException.class, "PRODUCT_OUT_OF_STOCK",
                        "Stock insuficiente para el producto 101"
                )
        );
    }

    public static Stream<ControllerRemoveCartItemScenario> provideControllerRemoveCartItemScenarios() {
        return Stream.of(
                new ControllerRemoveCartItemScenario(
                        "Elimina exitosamente el item del carrito y retorna 204 No Content",
                        10L, 204, null, null, null
                ),
                new ControllerRemoveCartItemScenario(
                        "Falla al intentar eliminar un item inexistente y retorna 404 Not Found",
                        999L, 404, CartNotFoundException.class, "CART_NOT_FOUND",
                        "No se encontró el ítem en el carrito con id: 999"
                )
        );
    }

    public static Stream<ControllerGetCartScenario> provideControllerGetCartScenarios() {
        return Stream.of(
                new ControllerGetCartScenario(
                        "Consulta exitosa de carrito por userId retorna 200 OK",
                        10L, 1L, 1L, 101L, 2, "50.00", "100.00", 200, null, null
                ),
                new ControllerGetCartScenario(
                        "Consulta de carrito inexistente por userId retorna 404 Not Found",
                        null, 99L, null, null, null, null, "0.00", 404,
                        CartNotFoundException.class, "No se encontró carrito para el usuario 99"
                )
        );
    }

    public static Stream<ControllerGetCartAuthScenario> provideControllerGetCartAuthScenarios() {
        return Stream.of(
                new ControllerGetCartAuthScenario(
                        "Consulta exitosa de carrito autenticado con header X-USER-ID retorna 200 OK",
                        10L, 1L, 1L, 101L, 2, "50.00", "100.00", 200, null, null
                ),
                new ControllerGetCartAuthScenario(
                        "Consulta de carrito sin header X-USER-ID retorna 401 Unauthorized",
                        null, null, null, null, null, null, "0.00", 401,
                        null, "Usuario no autenticado"
                ),
                new ControllerGetCartAuthScenario(
                        "Consulta de carrito autenticado inexistente retorna 404 Not Found",
                        null, 99L, null, null, null, null, "0.00", 404,
                        CartNotFoundException.class, "No se encontró carrito para el usuario 99"
                )
        );
    }

    public static Stream<ControllerApplyCouponScenario> provideControllerApplyCouponScenarios() {
        return Stream.of(
                new ControllerApplyCouponScenario(
                        "Aplicacion exitosa de cupon al carrito retorna 200 OK con descuento",
                        10L, 1L, "PROMO10", 1L, 101L, 2, "50.00", "100.00", "10.00", "90.00", 200, null, null
                ),
                new ControllerApplyCouponScenario(
                        "Aplicacion de cupon inexistente retorna 404 Not Found",
                        10L, 1L, "NOEXISTE", 0L, 0L, 0, "0.00", "0.00", "0.00", "0.00", 404,
                        CouponNotFoundException.class, "El cupón no existe"
                ),
                new ControllerApplyCouponScenario(
                        "Aplicacion de cupon invalido o expirado retorna 400 Bad Request",
                        10L, 1L, "EXPIRADO", 0L, 0L, 0, "0.00", "0.00", "0.00", "0.00", 400,
                        InvalidCouponException.class, "El cupón ha expirado"
                ),
                new ControllerApplyCouponScenario(
                        "Aplicacion de cupon a carrito inexistente retorna 404 Not Found",
                        0L, 99L, "PROMO10", 0L, 0L, 0, "0.00", "0.00", "0.00", "0.00", 404,
                        CartNotFoundException.class, "No se encontró carrito para el usuario 99"
                ),
                new ControllerApplyCouponScenario(
                        "Aplicacion de cupon a carrito vacio retorna 400 Bad Request",
                        10L, 1L, "PROMO10", 0L, 0L, 0, "0.00", "0.00", "0.00", "0.00", 400,
                        CartEmptyException.class, "No se puede aplicar un cupón a un carrito vacío"
                )
        );
    }

    // =========================================================================
    // CartService Scenarios
    // =========================================================================

    public static Stream<GetCartByUserIdScenario> provideGetCartByUserIdScenarios() {
        return Stream.of(
                new GetCartByUserIdScenario(
                        "Obtiene carrito existente con items para el usuario",
                        true, false, 10L, 1L, 101L, 2, "50.00", 1, "100.00"
                ),
                new GetCartByUserIdScenario(
                        "Crea y retorna carrito vacio mediante orElseGet cuando el usuario no tiene carrito",
                        false, false, null, 2L, null, null, null, 0, "0.00"
                ),
                new GetCartByUserIdScenario(
                        "Retorna carrito con lista vacia cuando el carrito en BD tiene items nulos",
                        true, true, 11L, 3L, null, null, null, 0, "0.00"
                )
        );
    }

    public static Stream<AddToCartScenario> provideAddToCartScenarios() {
        return Stream.of(
                new AddToCartScenario(
                        "Suma cantidades sin duplicar registro cuando el producto ya existe en el carrito",
                        10L, 1L, true, false, 1, 100L, 2, "50.00", 100L, 3, "50.00", 1, "250.00", null, null
                ),
                new AddToCartScenario(
                        "Agrega nuevo item a la lista cuando el producto no existia en el carrito",
                        10L, 1L, true, false, 1, 100L, 2, "50.00", 200L, 1, "30.00", 2, "130.00", null, null
                ),
                new AddToCartScenario(
                        "Crea un carrito nuevo y asigna el producto cuando el usuario no tenia carrito previo",
                        20L, 2L, false, false, 0, null, null, null, 300L, 4, "25.00", 1, "100.00", null, null
                ),
                new AddToCartScenario(
                        "Inicializa lista de items cuando el carrito existente tiene items nulos y agrega producto",
                        30L, 3L, true, true, 0, null, null, null, 100L, 2, "40.00", 1, "80.00", null, null
                ),
                new AddToCartScenario(
                        "Actualiza producto existente asumiendo cero cuando cantidades previa y solicitada son nulas",
                        10L, 1L, true, false, 1, 100L, null, "50.00", 100L, null, "50.00", 1, "0.00", null, null
                ),
                new AddToCartScenario(
                        "Ignora item previo con productId nulo en el filtro lambda y agrega el nuevo producto",
                        10L, 1L, true, false, 1, null, 1, "50.00", 100L, 2, "50.00", 2, "150.00", null, null
                ),
                new AddToCartScenario(
                        "Falla al intentar agregar al carrito un producto inexistente en el catalogo",
                        null, 1L, false, false, 0, null, null, null, 999L, 1, "0.00", 0, "0.00",
                        ProductNotFoundException.class, "Producto no encontrado con id: 999"
                ),
                new AddToCartScenario(
                        "Falla cuando la cantidad solicitada de un producto nuevo supera 5 unidades",
                        10L, 1L, true, false, 0, null, null, null, 101L, 6, "20.00", 0, "0.00",
                        BusinessOperationException.class, "Máximo 5 unidades por producto"
                ),
                new AddToCartScenario(
                        "Falla cuando la cantidad acumulada de un producto existente supera 5 unidades",
                        10L, 1L, true, false, 1, 101L, 3, "20.00", 101L, 3, "20.00", 0, "0.00",
                        BusinessOperationException.class, "Máximo 5 unidades por producto"
                ),
                new AddToCartScenario(
                        "Falla cuando el carrito ya tiene 10 lineas de productos diferentes y se intenta agregar otro",
                        10L, 1L, true, false, 10, 1L, 1, "10.00", 999L, 1, "10.00", 0, "0.00",
                        BusinessOperationException.class, "Máximo 10 líneas de productos diferentes"
                ),
                new AddToCartScenario(
                        "Falla cuando el monto total del carrito supera el limite permitido",
                        10L, 1L, true, false, 0, null, null, null, 101L, 5, "2000.00", 0, "0.00",
                        BusinessOperationException.class, "El total del carrito no puede superar $500"
                )
        );
    }

    public static Stream<UpdateCartItemQuantityScenario> provideUpdateCartItemQuantityScenarios() {
        return Stream.of(
                new UpdateCartItemQuantityScenario(
                        "Actualiza cantidad exitosamente usando precio vigente y conservando el itemId real de BD",
                        10L, 3, true, 1L, 1L, 10L, 101L, 1, "100.00", "120.00", 10, 1, "360.00", null, null
                ),
                new UpdateCartItemQuantityScenario(
                        "Falla cuando no existe ningun carrito con el itemId solicitado",
                        999L, 2, false, null, null, null, null, null, null, null, null, 0, "0.00",
                        CartNotFoundException.class, "No se encontró el ítem en el carrito con id: 999"
                ),
                new UpdateCartItemQuantityScenario(
                        "Falla cuando el carrito devuelto no contiene el itemId en su coleccion de items",
                        999L, 2, true, 1L, 1L, 10L, 101L, 1, "100.00", null, null, 0, "0.00",
                        CartNotFoundException.class, "No se encontró el ítem en el carrito con id: 999"
                ),
                new UpdateCartItemQuantityScenario(
                        "Falla cuando la nueva cantidad solicitada supera el maximo de 5 unidades por producto",
                        10L, 6, true, 1L, 1L, 10L, 101L, 1, "100.00", null, null, 0, "0.00",
                        BusinessOperationException.class, "Máximo 5 unidades por producto"
                ),
                new UpdateCartItemQuantityScenario(
                        "Falla cuando el stock disponible en el catalogo es menor a la cantidad solicitada",
                        10L, 4, true, 1L, 1L, 10L, 101L, 1, "100.00", "120.00", 2, 0, "0.00",
                        ProductOutOfStockException.class, "Stock insuficiente para el producto 101"
                ),
                new UpdateCartItemQuantityScenario(
                        "Falla cuando el nuevo total recalculado del carrito supera el tope permitido",
                        10L, 3, true, 1L, 1L, 10L, 101L, 1, "100.00", "2000.00", 10, 0, "0.00",
                        BusinessOperationException.class, "El total del carrito no puede superar $500"
                )
        );
    }

    public static Stream<RemoveCartItemScenario> provideRemoveCartItemScenarios() {
        return Stream.of(
                new RemoveCartItemScenario(
                        "Elimina exitosamente el item de la coleccion del carrito y persiste los cambios",
                        10L, true, 1L, 1L, 10L, 101L, 2, "50.00", 0, null, null
                ),
                new RemoveCartItemScenario(
                        "Falla al intentar eliminar un itemId cuando no existe carrito asociado",
                        999L, false, null, null, null, null, null, null, 0,
                        CartNotFoundException.class, "No se encontró el ítem en el carrito con id: 999"
                ),
                new RemoveCartItemScenario(
                        "Falla cuando el itemId no coincide con ningun elemento dentro de la lista del carrito",
                        999L, true, 1L, 1L, 10L, 101L, 2, "50.00", 1,
                        CartNotFoundException.class, "No se encontró el ítem en el carrito con id: 999"
                )
        );
    }

    public static Stream<GetCartScenario> provideGetCartScenarios() {
        return Stream.of(
                new GetCartScenario(
                        "Obtiene carrito exitosamente para usuario autenticado",
                        true, 10L, 1L, 101L, 2, "50.00", 1, "100.00", null, null
                ),
                new GetCartScenario(
                        "Falla al obtener carrito cuando el usuario autenticado no tiene carrito",
                        false, null, 99L, 0L, 0, "0.00", 0, "0.00",
                        CartNotFoundException.class, "No se encontró carrito para el usuario 99"
                )
        );
    }
}
