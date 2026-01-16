package com.offlix.distributed_graph_engine.graph.operations.scc;

import com.offlix.distributed_graph_engine.domain.GraphType;
import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.Map;
import java.util.Set;

public class UndirectedSccFinderStrategy<T> implements SccFinderStrategy<T> {
    private final GraphContext<T> context;

    public UndirectedSccFinderStrategy(GraphContext<T> context) {
        this.context = context;
    }

    @Override
    public Map<Integer, Set<T>> find() {
        return Map.of();
    }
}
