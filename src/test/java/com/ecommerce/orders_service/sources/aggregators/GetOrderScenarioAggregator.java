package com.ecommerce.orders_service.sources.aggregators;

import com.ecommerce.orders_service.model.OrderStatus;
import com.ecommerce.orders_service.sources.scenarios.GetOrderScenario;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * Aggregator de JUnit 5 encargado de transformar una fila del CSV {@code service_get_order_scenarios.csv}
 * en una instancia inmutable de {@link GetOrderScenario}.
 */
public class GetOrderScenarioAggregator implements ArgumentsAggregator {

    @Override
    public GetOrderScenario aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
            throws ArgumentsAggregationException {

        return new GetOrderScenario(
                accessor.getString(0),
                accessor.getLong(1),
                Boolean.TRUE.equals(accessor.getBoolean(2)),
                accessor.getLong(3),
                accessor.getString(4),
                parseOrderStatus(accessor.getString(5)),
                accessor.getLong(6),
                accessor.getInteger(7),
                accessor.getString(8),
                parseExceptionClass(accessor.getString(9)),
                accessor.getString(10)
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
