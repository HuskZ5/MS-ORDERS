package com.ecommerce.orders_service.sources.aggregators;

import com.ecommerce.orders_service.sources.scenarios.ControllerGetOrderScenario;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * Aggregator de JUnit 5 encargado de transformar una fila del CSV {@code controller_get_order_scenarios.csv}
 * en una instancia inmutable de {@link ControllerGetOrderScenario}.
 */
public class ControllerGetOrderScenarioAggregator implements ArgumentsAggregator {

    @Override
    public ControllerGetOrderScenario aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
            throws ArgumentsAggregationException {

        return new ControllerGetOrderScenario(
                accessor.getString(0),
                accessor.getLong(1),
                accessor.getLong(2),
                accessor.getString(3),
                accessor.getString(4),
                accessor.getLong(5),
                accessor.getLong(6),
                accessor.getInteger(7),
                accessor.getString(8),
                accessor.getInteger(9),
                parseExceptionClass(accessor.getString(10)),
                accessor.getString(11)
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
