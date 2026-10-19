package com.ecommerce.orders_service.sources.aggregators;

import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.sources.scenarios.CancelOrderScenario;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * Aggregator de JUnit 5 encargado de transformar una fila del CSV {@code cancel_order_scenarios.csv}
 * en una instancia fuertemente tipada e inmutable de {@link CancelOrderScenario}.
 */
public class CancelOrderScenarioAggregator implements ArgumentsAggregator {

    @Override
    public CancelOrderScenario aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
            throws ArgumentsAggregationException {

        return new CancelOrderScenario(
                accessor.getString(0),
                accessor.getLong(1),
                Boolean.TRUE.equals(accessor.getBoolean(2)),
                parseOrderStatus(accessor.getString(3)),
                accessor.getLong(4),
                accessor.getLong(5),
                accessor.getString(6),
                accessor.getLong(7),
                accessor.getInteger(8),
                accessor.getString(9),
                Boolean.TRUE.equals(accessor.getBoolean(10)),
                parseOrderStatus(accessor.getString(11)),
                parseExceptionClass(accessor.getString(12)),
                accessor.getString(13)
        );
    }

    private OrderStatus parseOrderStatus(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return OrderStatus.valueOf(rawValue.trim());
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
