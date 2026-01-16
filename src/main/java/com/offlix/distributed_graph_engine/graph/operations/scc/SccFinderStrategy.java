package com.offlix.distributed_graph_engine.graph.operations.scc;

import java.util.Map;
import java.util.Set;

public interface SccFinderStrategy<T> {
    public Map<Integer, Set<T>> find();
}
