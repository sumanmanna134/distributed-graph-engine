package com.offlix.distributed_graph_engine.graph.operations.traversal;

import com.offlix.distributed_graph_engine.graph.core.GraphContext;

import java.util.Set;
import java.util.function.Consumer;

public interface GraphTraversal<T> {
    void traverse(GraphContext<T> context, T startNode, Consumer<T> action, Set<T> visited);

}
