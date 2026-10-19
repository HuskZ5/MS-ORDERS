package com.ecommerce.orders_service.sources.providers;

import com.ecommerce.orders_service.exceptions.CartEmptyException;
import com.ecommerce.orders_service.exceptions.CartNotFoundException;
import com.ecommerce.orders_service.exceptions.CouponNotFoundException;
import com.ecommerce.orders_service.exceptions.InvalidCouponException;
import com.ecommerce.orders_service.sources.scenarios.coupon.ApplyCouponScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.CalculateDiscountScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.OrderCouponOperationsScenario;
import com.ecommerce.orders_service.sources.scenarios.coupon.ValidateCouponScenario;

import java.util.stream.Stream;

/**
 * Proveedor centralizado de escenarios de prueba para el dominio de Cupones (CouponService).
 */
public final class CouponScenarioProvider {

    private CouponScenarioProvider() {
    }

    public static Stream<ValidateCouponScenario> provideValidateCouponScenarios() {
        return Stream.of(
                new ValidateCouponScenario(
                        "Valida exitosamente un cupon vigente sin limites ni compra minima",
                        "PROMO10", 1L, "100.00", true, 1L, "10.00", true, -1, 5,
                        null, null, null, null, null, false, null, null
                ),
                new ValidateCouponScenario(
                        "Valida exitosamente un cupon vigente con limites y compra minima cumplidos",
                        "PROMO20", 1L, "150.00", true, 2L, "20.00", true, -1, 5,
                        "100.00", 10, 3L, 2, 1L, true, null, null
                ),
                new ValidateCouponScenario(
                        "Falla cuando el codigo del cupon es nulo",
                        null, 1L, "100.00", false, 0L, "0.00", false, 0, 0,
                        null, null, null, null, null, false,
                        InvalidCouponException.class, "Debes proporcionar un código de cupón para aplicarlo"
                ),
                new ValidateCouponScenario(
                        "Falla cuando el codigo del cupon esta en blanco",
                        "   ", 1L, "100.00", false, 0L, "0.00", false, 0, 0,
                        null, null, null, null, null, false,
                        InvalidCouponException.class, "Debes proporcionar un código de cupón para aplicarlo"
                ),
                new ValidateCouponScenario(
                        "Falla cuando el cupon no existe en el repositorio",
                        "NOEXISTE", 1L, "100.00", false, 0L, "0.00", false, 0, 0,
                        null, null, null, null, null, false,
                        CouponNotFoundException.class, "El cupón no existe"
                ),
                new ValidateCouponScenario(
                        "Falla cuando el cupon se encuentra inactivo",
                        "INACTIVO", 1L, "100.00", true, 3L, "10.00", false, -1, 5,
                        null, null, null, null, null, false,
                        InvalidCouponException.class, "El cupón se encuentra inactivo"
                ),
                new ValidateCouponScenario(
                        "Falla cuando la fecha de inicio del cupon es posterior a la actual",
                        "FUTURO", 1L, "100.00", true, 4L, "10.00", true, 2, 5,
                        null, null, null, null, null, false,
                        InvalidCouponException.class, "El cupón todavía no está vigente"
                ),
                new ValidateCouponScenario(
                        "Falla cuando la fecha de expiracion del cupon ya paso",
                        "EXPIRADO", 1L, "100.00", true, 5L, "10.00", true, -5, -1,
                        null, null, null, null, null, false,
                        InvalidCouponException.class, "El cupón ha expirado"
                ),
                new ValidateCouponScenario(
                        "Falla cuando el subtotal no alcanza la compra minima requerida",
                        "MINIMO", 1L, "50.00", true, 6L, "10.00", true, -1, 5,
                        "100.00", null, null, null, null, false,
                        InvalidCouponException.class, "El carrito no alcanza la compra mínima requerida de $100.00"
                ),
                new ValidateCouponScenario(
                        "Falla cuando el cupon alcanzo su limite global de usos",
                        "GLOBALMAX", 1L, "150.00", true, 7L, "10.00", true, -1, 5,
                        "100.00", 5, 5L, null, null, false,
                        InvalidCouponException.class, "El cupón ha alcanzado su límite global de usos"
                ),
                new ValidateCouponScenario(
                        "Falla cuando el usuario alcanzo el limite de usos permitidos para el cupon",
                        "USERMAX", 1L, "150.00", true, 8L, "10.00", true, -1, 5,
                        "100.00", 10, 2L, 2, 2L, false,
                        InvalidCouponException.class,
                        "El usuario ha alcanzado el límite de usos permitidos para este cupón"
                )
        );
    }

    public static Stream<ApplyCouponScenario> provideApplyCouponScenarios() {
        return Stream.of(
                new ApplyCouponScenario(
                        "Aplica exitosamente el primer cupon a un carrito con productos",
                        1L, 10L, true, false, false, 101L, 2, "50.00",
                        1L, "PROMO10", "10.00", false, false, false, false, null, null
                ),
                new ApplyCouponScenario(
                        "Aplica exitosamente un cupon acumulable cuando el carrito ya tiene un cupon acumulable",
                        1L, 10L, true, false, false, 101L, 2, "50.00",
                        2L, "STACK10", "10.00", true, false, true, true, null, null
                ),
                new ApplyCouponScenario(
                        "Falla al aplicar cupon cuando el usuario no tiene carrito registrado",
                        99L, 0L, false, false, false, 0L, 0, "0.00",
                        1L, "PROMO10", "10.00", false, false, false, false,
                        CartNotFoundException.class, "No se encontró carrito para el usuario 99"
                ),
                new ApplyCouponScenario(
                        "Falla al aplicar cupon cuando la lista de items del carrito es nula",
                        1L, 10L, true, true, false, 0L, 0, "0.00",
                        1L, "PROMO10", "10.00", false, false, false, false,
                        CartEmptyException.class, "No se puede aplicar un cupón a un carrito vacío"
                ),
                new ApplyCouponScenario(
                        "Falla al aplicar cupon cuando la lista de items del carrito esta vacia",
                        1L, 10L, true, false, true, 0L, 0, "0.00",
                        1L, "PROMO10", "10.00", false, false, false, false,
                        CartEmptyException.class, "No se puede aplicar un cupón a un carrito vacío"
                ),
                new ApplyCouponScenario(
                        "Falla cuando el cupon ya se encuentra aplicado al carrito",
                        1L, 10L, true, false, false, 101L, 2, "50.00",
                        1L, "PROMO10", "10.00", true, true, false, false,
                        InvalidCouponException.class, "El cupón ya se encuentra aplicado al carrito"
                ),
                new ApplyCouponScenario(
                        "Falla cuando se intenta aplicar un cupon no acumulable habiendo otro cupon en el carrito",
                        1L, 10L, true, false, false, 101L, 2, "50.00",
                        2L, "NOSTACK", "15.00", false, false, true, true,
                        InvalidCouponException.class, "El cupón no puede acumularse con otros cupones"
                ),
                new ApplyCouponScenario(
                        "Falla cuando el carrito contiene un cupon previo que no permite acumulacion",
                        1L, 10L, true, false, false, 101L, 2, "50.00",
                        2L, "STACK10", "10.00", true, false, true, false,
                        InvalidCouponException.class, "El carrito contiene un cupón que no permite acumulación"
                )
        );
    }

    public static Stream<CalculateDiscountScenario> provideCalculateDiscountScenarios() {
        return Stream.of(
                new CalculateDiscountScenario(
                        "Retorna descuento cero y total intacto cuando el carrito no tiene cupones aplicados",
                        10L, "200.00", null, null, "0.00", "200.00"
                ),
                new CalculateDiscountScenario(
                        "Calcula descuento porcentual y total final con un cupon aplicado",
                        10L, "200.00", "15.00", null, "30.00", "170.00"
                ),
                new CalculateDiscountScenario(
                        "Calcula descuento acumulado con dos cupones sin superar el subtotal",
                        10L, "200.00", "10.00", "15.00", "50.00", "150.00"
                ),
                new CalculateDiscountScenario(
                        "Limita el descuento maximo al subtotal cuando la suma de porcentajes supera el 100 por ciento",
                        10L, "100.00", "60.00", "50.00", "100.00", "0.00"
                )
        );
    }

    public static Stream<OrderCouponOperationsScenario> provideOrderCouponOperationsScenarios() {
        return Stream.of(
                new OrderCouponOperationsScenario(
                        "Valida cupones aplicados y registra el uso eliminando los cupones del carrito al crear la orden",
                        10L, 99L, 1L, "150.00", 1L, "PROMO10", "10.00", 1
                ),
                new OrderCouponOperationsScenario(
                        "Procesa orden sin cupones aplicados limpiando asociacion del carrito",
                        10L, 100L, 2L, "200.00", null, null, null, 0
                )
        );
    }
}
