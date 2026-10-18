package com.ecommerce.orders_service.sources.aggregators;

import com.ecommerce.orders_service.sources.scenarios.GetCartByUserIdScenario;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * Aggregator de JUnit 5 encargado de transformar una fila del CSV {@code service_get_cart_by_user_id_scenarios.csv}
 * en una instancia inmutable de {@link GetCartByUserIdScenario}.
 */
public class GetCartByUserIdScenarioAggregator implements ArgumentsAggregator {

    @Override
    public GetCartByUserIdScenario aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
            throws ArgumentsAggregationException {

        return new GetCartByUserIdScenario(
                accessor.getString(0),
                Boolean.TRUE.equals(accessor.getBoolean(1)),
                Boolean.TRUE.equals(accessor.getBoolean(2)),
                accessor.getLong(3),
                accessor.getLong(4),
                accessor.getLong(5),
                accessor.getInteger(6),
                accessor.getString(7),
                accessor.getInteger(8),
                accessor.getString(9)
        );
    }
}
