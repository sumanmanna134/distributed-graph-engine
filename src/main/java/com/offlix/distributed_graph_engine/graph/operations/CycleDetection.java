package com.offlix.distributed_graph_engine.graph.operations;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;
import com.offlix.distributed_graph_engine.graph.operations.cycle.CycleStrategy;
import com.offlix.distributed_graph_engine.graph.operations.cycle.DirectedCycleStrategy;
import com.offlix.distributed_graph_engine.graph.operations.cycle.UndirectedCycleStrategy;

import java.util.*;

public class CycleDetection<T> {
    private final GraphContext<T> context;
    private final Map<GraphType, CycleStrategy<T>> strategies = new HashMap<>();

    public CycleDetection(GraphContext<T> context){
        this.context = context;
        strategies.put(GraphType.DIRECTED, new DirectedCycleStrategy<>(context));
        strategies.put(GraphType.UNDIRECTED, new UndirectedCycleStrategy<>(context));

    }

    public boolean containCycle(){
        CycleStrategy<T> strategy = strategies.get(context.getType());
        if(strategy==null){
            throw new UnsupportedOperationException("No strategy for type: " + context.getType());
        }

        return strategy.containCycle();
    }
}
