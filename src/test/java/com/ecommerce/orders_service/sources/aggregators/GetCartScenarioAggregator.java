package com.ecommerce.orders_service.sources.aggregators;

import com.ecommerce.orders_service.sources.scenarios.GetCartScenario;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * Aggregator de JUnit 5 encargado de transformar una fila del CSV {@code service_get_cart_scenarios.csv}
 * en una instancia inmutable de {@link GetCartScenario}.
 */
public class GetCartScenarioAggregator implements ArgumentsAggregator {

    @Override
    public GetCartScenario aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
            throws ArgumentsAggregationException {

        return new GetCartScenario(
                accessor.getString(0),
                Boolean.TRUE.equals(accessor.getBoolean(1)),
                accessor.getLong(2),
                accessor.getLong(3),
                accessor.getLong(4),
                accessor.getInteger(5),
                accessor.getString(6),
                accessor.getInteger(7),
                accessor.getString(8),
                parseExceptionClass(accessor.getString(9)),
                accessor.getString(10)
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
