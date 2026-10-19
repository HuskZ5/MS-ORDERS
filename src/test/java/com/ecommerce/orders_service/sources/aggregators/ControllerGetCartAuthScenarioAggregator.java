package com.ecommerce.orders_service.sources.aggregators;

import com.ecommerce.orders_service.sources.scenarios.ControllerGetCartAuthScenario;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * Aggregator de JUnit 5 encargado de transformar una fila del CSV {@code controller_get_cart_auth_scenarios.csv}
 * en una instancia fuertemente tipada e inmutable de {@link ControllerGetCartAuthScenario}.
 */
public class ControllerGetCartAuthScenarioAggregator implements ArgumentsAggregator {

    @Override
    public ControllerGetCartAuthScenario aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
            throws ArgumentsAggregationException {

        return new ControllerGetCartAuthScenario(
                accessor.getString(0),
                accessor.getLong(1),
                accessor.getLong(2),
                accessor.getLong(3),
                accessor.getLong(4),
                accessor.getInteger(5),
                accessor.getString(6),
                accessor.getString(7),
                accessor.getInteger(8),
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
