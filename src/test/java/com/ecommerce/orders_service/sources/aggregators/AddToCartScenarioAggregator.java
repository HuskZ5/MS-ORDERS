package com.ecommerce.orders_service.sources.aggregators;

import com.ecommerce.orders_service.sources.scenarios.AddToCartScenario;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * Aggregator de JUnit 5 encargado de transformar una fila del CSV {@code service_add_to_cart_scenarios.csv}
 * en una instancia inmutable de {@link AddToCartScenario}.
 */
public class AddToCartScenarioAggregator implements ArgumentsAggregator {

    @Override
    public AddToCartScenario aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
            throws ArgumentsAggregationException {

        return new AddToCartScenario(
                accessor.getString(0),
                accessor.getLong(1),
                accessor.getLong(2),
                Boolean.TRUE.equals(accessor.getBoolean(3)),
                Boolean.TRUE.equals(accessor.getBoolean(4)),
                accessor.getLong(5),
                accessor.getInteger(6),
                accessor.getString(7),
                accessor.getLong(8),
                accessor.getInteger(9),
                accessor.getString(10),
                accessor.getInteger(11),
                accessor.getString(12),
                parseExceptionClass(accessor.getString(13)),
                accessor.getString(14)
        );
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Throwable> parseExceptionClass(String className) throws ArgumentsAggregationException {
        if (className == null || className.isBlank()) {
            return null;
        }
        try {
            return (Class<? extends Throwable>) Class.forName(className.trim());
        } catch (ClassNotFoundException e) {
            throw new ArgumentsAggregationException("No se pudo cargar la clase de excepción: " + className, e);
        }
    }
}
