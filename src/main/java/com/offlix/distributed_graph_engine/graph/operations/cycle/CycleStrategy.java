package com.offlix.distributed_graph_engine.graph.operations.cycle;

import java.util.List;

public interface CycleStrategy<T> {
    public List<List<T>> findCycles();
}
