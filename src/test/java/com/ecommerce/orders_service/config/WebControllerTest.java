package com.ecommerce.orders_service.config;

import com.ecommerce.orders_service.exceptions.GlobalExceptionHandler;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-anotación para pruebas web MVC que encapsula el boilerplate de Spring Boot.
 * <p>
 * Combina {@link WebMvcTest}, {@link AutoConfigureMockMvc}, {@link ActiveProfiles} y
 * el registro automático del manejador global {@link GlobalExceptionHandler}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("local")
@Import(GlobalExceptionHandler.class)
public @interface WebControllerTest {

    @AliasFor(annotation = WebMvcTest.class, attribute = "controllers")
    Class<?>[] controllers() default {};

    @AliasFor(annotation = WebMvcTest.class, attribute = "value")
    Class<?>[] value() default {};
}
