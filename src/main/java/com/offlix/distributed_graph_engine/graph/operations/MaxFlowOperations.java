package com.offlix.distributed_graph_engine.graph.operations;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.Map;

public class MaxFlowOperations<T> {
    private final GraphContext<T> context;
    public MaxFlowOperations(GraphContext<T> context){
        this.context = context;
    }

    public double getOutgoingCapacity(T node){
        return context.getNeighborsWithEdgeWeight(node)
                .map(this::edgeCost)
                .orElse(0.0);
    }


    private Double edgeCost(Map<T, Double> nei) {
        return nei.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
